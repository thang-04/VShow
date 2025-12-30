package com.vticket.identity.app.usercase;

import com.google.gson.Gson;
import com.vticket.commonlibs.utils.CommonUtils;
import com.vticket.commonlibs.utils.Constant;
import com.vticket.commonlibs.utils.ResponseJson;
import io.micrometer.common.util.StringUtils;
import com.vticket.identity.app.dto.req.OtpVerifyRequest;
import com.vticket.identity.app.dto.req.RegisterRequest;
import com.vticket.identity.app.dto.res.LoginResponse;
import com.vticket.identity.app.dto.res.TokenResponse;
import com.vticket.identity.app.mapper.IdentityDtoMapper;
import com.vticket.identity.domain.entity.Role;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.jwt.JwtService;
import com.vticket.identity.infra.messaging.EmailEventPublisher;
import com.vticket.identity.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private static final SecureRandom secureRandom = new SecureRandom();
    private final Gson gson;
    private final PasswordEncoder passwordEncoder;
    private final IdentityDtoMapper identityDtoMapper;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final OtpUseCase otpUseCase;

    public String executeSendOtp(RegisterRequest request) {
        String prefix = "[RegisterUseCase]|executeSendOtp|request=" + gson.toJson(request);
        log.info(prefix);
        try {
            // Validate email format
            if (StringUtils.isBlank(request.getEmail())) {
                log.error("{}|Email is required", prefix);
                return null;
            }
            if (!CommonUtils.isEmail(request.getEmail())) {
                log.error("{}|Invalid email format: {}", prefix, request.getEmail());
                return null;
            }
            // Validate username
            if (StringUtils.isBlank(request.getUsername())) {
                log.error("{}|Username is required", prefix);
                return null;
            }
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                log.error("{}|Username already exists: {}", prefix, request.getUsername());
                return null;
            }
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                log.error("{}|Email already exists: {}", prefix, request.getEmail());
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
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            user.updateTokens(accessToken, refreshToken);

            // send otp and store pending user in redis
            if (otpUseCase.sendRegistrationOtp(user)) {
                String emailKey = user.getEmail() == null ? null : user.getEmail().trim().toLowerCase();
                String pendingKey = String.format(Constant.RedisKey.PENDING_USER_EMAIL, emailKey);
                redisService.getRedisStringTemplate().opsForValue().set(pendingKey, gson.toJson(user));
                redisService.getRedisStringTemplate().expire(pendingKey, 10L, TimeUnit.MINUTES);//10p
                log.info("Stored pending user for email {}. Waiting for OTP verification.", user.getEmail());
                return ResponseJson.success("Send otp verification successfully");
            }
        } catch (Exception e) {
            log.error("{}|Exception={}", prefix, e.getMessage());
        }
        return null;
    }

    public LoginResponse executeInsert(OtpVerifyRequest request) {
        String prefix = "[RegisterUseCase]|executeInsert|request=" + gson.toJson(request);
        String emailKey = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        try {
            String keyOtp = String.format(Constant.RedisKey.OTP_EMAIL, emailKey);
            String cachedOtp = redisService.getRedisStringTemplate().opsForValue().get(keyOtp);
            if (cachedOtp != null) {
                User savedUser = otpUseCase.verifyOtp(request);
                if (savedUser != null) {
                    log.info("{}|Register successfully|User={}", prefix, savedUser);
                    TokenResponse tokens = TokenResponse.builder()
                            .accessToken(savedUser.getAccessToken())
                            .refreshToken(savedUser.getRefreshToken())
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
                }
            }
        } catch (Exception e) {
            log.error("{}|Exception={}", prefix, e.getMessage());
        }
        return null;
    }
}

