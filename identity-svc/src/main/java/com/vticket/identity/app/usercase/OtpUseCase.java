package com.vticket.identity.app.usercase;

import com.vticket.commonlibs.utils.Constant;
import com.vticket.identity.app.dto.req.OtpVerifyRequest;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.event.EmailEvent;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.messaging.EmailEventPublisher;
import com.vticket.identity.infra.redis.RedisService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpUseCase {
    private static final SecureRandom secureRandom = new SecureRandom();
    private final RedisService redisService;
    private final EmailEventPublisher emailEventPublisher;
    private final UserRepository userRepository;
    private final MessageService messageService;

    private String generateOtp() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    public boolean sendRegistrationOtp(User user) {
        String prefix = "[sendRegistrationOtp]";
        long start = System.currentTimeMillis();
        String otp = generateOtp();
        String emailKey = user.getEmail() == null ? null : user.getEmail().trim().toLowerCase();
        String key = String.format(Constant.RedisKey.OTP_EMAIL, emailKey);

        try {
            String cachedOtp = redisService.getRedisStringTemplate().opsForValue().get(key);
            if (!StringUtils.isEmpty(cachedOtp)) {
                log.error("{}|OTP has been existed by email:{}", prefix, user.getEmail());
                return false;
            }
            //build Event
            EmailEvent event = buildEmailOtpEvent(user.getEmail(), messageService.get("email.otp.subject"), messageService.get("email.template.otp"), Map.of("otp", otp));
            //publish event to RabbitMQ
            emailEventPublisher.publishOtp(event);

            log.info("{}|OTP event published for email {} in {} ms", prefix,
                    user.getEmail(), System.currentTimeMillis() - start);
            //cache redis
            redisService.getRedisStringTemplate().opsForValue().set(key, otp);
            redisService.getRedisStringTemplate().expire(key, 5L, TimeUnit.MINUTES); // 5p
            log.info("{}|Stored OTP in Redis for user ID {} with time {} ms", prefix, user.getId(), (System.currentTimeMillis() - start));
            return true;
        } catch (Exception e) {
            log.error("{}|Failed to send OTP to email {}: {}", prefix, user.getEmail(), e.getMessage());
            return false;
        }
    }

    public User verifyOtp(OtpVerifyRequest request) {
        String prefix = "[verifyOtp]";
        long start = System.currentTimeMillis();
        String emailKey = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String key = String.format(Constant.RedisKey.OTP_EMAIL, emailKey);

        try {
            String cachedOtp = redisService.getRedisStringTemplate().opsForValue().get(key);
            log.info("{}|Retrieved OTP from Redis for email {} and otp {} with time {}ms", prefix, request.getEmail(), cachedOtp, (System.currentTimeMillis() - start));
            if (StringUtils.isEmpty(cachedOtp)) {
                log.error("{}|No OTP found or OTP expired for email {}", prefix, request.getEmail());
                return null; // OTP expired or not found
            }

            if (request.getOtp() != null && request.getOtp().equals(cachedOtp)) {
                // fetch pending user by full email
                String pendingKey = String.format(Constant.RedisKey.PENDING_USER_EMAIL, emailKey);
                String pendingUserJson = redisService.getRedisStringTemplate().opsForValue().get(pendingKey);
                if (StringUtils.isEmpty(pendingUserJson)) {
                    log.error("{}|No pending user found for email {} despite valid OTP", prefix, request.getEmail());
                    return null;
                }
                User user = gson.fromJson(pendingUserJson, User.class);
                // insert user to db
                User savedUser = userRepository.save(user);
                // cache user info directly and cleanup keys
                String userKey = Constant.RedisKey.USER_ID + savedUser.getId();
                redisService.getRedisStringTemplate().opsForValue().set(userKey, gson.toJson(savedUser));
                redisService.getRedisStringTemplate().expire(userKey, 30L, TimeUnit.MINUTES);//30p
                // cleanup pending and otp key
                redisService.getRedisStringTemplate().delete(pendingKey);
                redisService.getRedisStringTemplate().delete(key);
                log.info("{}|OTP verified and user inserted for email {} with userId {}", prefix, request.getEmail(), savedUser.getId());
                return user;
            } else {
                log.error("{}|Invalid OTP for email {}", prefix, request.getEmail());
                return null;
            }
        } catch (Exception e) {
            log.error("{}|Error verifying OTP for email {}: {}", prefix, request.getEmail(), e.getMessage());
            return null;
        }
    }

    public boolean resendRegistrationOtp(OtpVerifyRequest request) {
        String prefix = "[resendRegistrationOtp]";
        long start = System.currentTimeMillis();
        String localPart = request.getEmail().split("@")[0];
        String key = String.format(Constant.RedisKey.OTP_EMAIL, localPart);

        try {
            String cachedOtp = redisService.getRedisStringTemplate().opsForValue().get(key);
            String otp;
            if (StringUtils.isEmpty(cachedOtp)) {
                otp = generateOtp();
                redisService.getRedisStringTemplate().opsForValue().set(key, otp);
                redisService.getRedisStringTemplate().expire(key, 5L, TimeUnit.MINUTES);
                log.info("{}|Generated NEW OTP for email {} after expiration", prefix, request.getEmail());
            } else {
                otp = cachedOtp;
                log.info("{}|Reusing EXISTING OTP for email {} still valid", prefix, request.getEmail());
            }

            //build Event
            EmailEvent event = buildEmailOtpEvent(request.getEmail(), messageService.get("email.otp.subject"), messageService.get("email.template.otp"), Map.of("otp", otp));
            //publish to rabbitmq
            emailEventPublisher.publishResendOtp(event);
            log.info("{}|Re-sent OTP to email {} with time {}ms", prefix,
                    request.getEmail(), (System.currentTimeMillis() - start));
            return true;
        } catch (Exception e) {
            log.error("{}|Failed to re-send OTP to email {}: {}", prefix, request.getEmail(), e.getMessage());
            return false;
        }
    }

    public EmailEvent buildEmailOtpEvent(String email, String subject, String template, Map<String, Object> otp) {
        return EmailEvent.builder()
                .to(email)
                .subject(subject)
                .template(template)
                .variables(otp)
                .build();
    }
}
