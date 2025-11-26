package com.vticket.identity.app.usercase;

import com.google.gson.Gson;
import com.vticket.commonlibs.utils.Constant;
import com.vticket.identity.app.dto.req.RegisterRequest;
import com.vticket.identity.app.dto.res.LoginResponse;
import com.vticket.identity.app.dto.res.TokenResponse;
import com.vticket.identity.domain.entity.Role;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.event.EmailEvent;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.jwt.JwtService;
import com.vticket.identity.infra.messaging.EmailEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private static final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailEventPublisher  emailEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final Gson gson;
//    private RedisService redisService;

    public LoginResponse execute(RegisterRequest request) {
        String prefix = "[LoginUseCase]|request=" + gson.toJson(request);
        log.info(prefix);
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                log.error("{}|Username={} already existed", prefix, request.getUsername());
                return null;
            }
            if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
                log.error("{}|Email={} already existed", prefix, request.getEmail());
                return null;
            }
            User user = User.builder()
                    .id(UUID.randomUUID().toString())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .deviceId(request.getDeviceId())
                    .roles(Set.of(Role.USER)) // Default role USER
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            user.updateTokens(accessToken, refreshToken);
            User savedUser = userRepository.save(user);

            // send otp and store pending user in redis
            if (sendRegistrationOtp(user)) {
                String emailKey = user.getEmail() == null ? null : user.getEmail().trim().toLowerCase();
                String pendingKey = String.format(Constant.RedisKey.PENDING_USER_EMAIL, emailKey);
//                redisService.getRedisSsoUser().opsForValue().set(pendingKey, gson.toJson(user));
//                redisService.getRedisSsoUser().expire(pendingKey, 10L, TimeUnit.MINUTES);//10p
                log.info("Stored pending user for email {}. Waiting for OTP verification.", user.getEmail());
                return null;
            }

            log.info("{}|Register successfully|User={}", prefix, savedUser);
            TokenResponse tokens = TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(ChronoUnit.SECONDS.between(Instant.now(),
                            Instant.now().plus(60, ChronoUnit.MINUTES)))
                    .build();
            return LoginResponse.builder()
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .tokens(tokens)
                    .build();
        } catch (Exception e) {
            log.error("{}|Exception={}", prefix, e.getMessage());
            return null;
        }
    }

    public boolean sendRegistrationOtp(User user) {
        long start = System.currentTimeMillis();
        String otp = generateOtp();
        String emailKey = user.getEmail() == null ? null : user.getEmail().trim().toLowerCase();
        String key = String.format(Constant.RedisKey.OTP_EMAIL, emailKey);

        try {
            //build Event
            EmailEvent event = EmailEvent.builder()
                    .to(user.getEmail())
                    .subject("Your OTP Code")
                    .template("otp-template")
                    .variables(Map.of("otp", otp))
                    .build();

            //publish event to RabbitMQ
            emailEventPublisher.publish(event);

            log.info("OTP event published for email {} in {}ms",
                    user.getEmail(), System.currentTimeMillis() - start);
            //cache redis
//            redisService.getRedisSsoUser().opsForValue().set(key, otp);
//            redisService.getRedisSsoUser().expire(key, 5L, TimeUnit.MINUTES); // 5p
//            log.info("Stored OTP in Redis for user ID {} with time {}", user.getId(), (System.currentTimeMillis() - start));
            return true;
        } catch (Exception e) {
            log.error("Failed to send OTP to email {}: {}", user.getEmail(), e.getMessage());
        }
        return false;
    }

    private String generateOtp() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }

}

