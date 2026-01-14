package com.vticket.identity.app.usercase;

import com.vticket.commonlibs.utils.CommonUtils;
import com.vticket.commonlibs.utils.Constant;
import com.vticket.identity.app.dto.req.OtpVerifyRequest;
import com.vticket.identity.app.dto.req.PasswordResetConfirmRequest;
import com.vticket.identity.app.dto.req.PasswordResetRequest;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.redis.RedisService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetUseCase {

    private final UserRepository userRepository;
    private final OtpUseCase otpUseCase;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    public boolean sendPasswordResetOtp(PasswordResetRequest request) {
        String prefix = "[PasswordResetUseCase]|sendPasswordResetOtp";
        log.info("{}|Password reset request for email: {}", prefix, request.getEmail());
        try {
            // Validate email format
            if (StringUtils.isBlank(request.getEmail())) {
                log.error("{}|Email is required", prefix);
                return false;
            }
            if (!CommonUtils.isEmail(request.getEmail())) {
                log.error("{}|Invalid email format: {}", prefix, request.getEmail());
                return false;
            }

            // Check if user exists
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.error("{}|User not found with email: {}", prefix, request.getEmail());
                // Don't reveal if email exists or not for security
                return false;
            }

            User user = userOptional.get();
            if (!user.isActive()) {
                log.error("{}|User account is not active: {}", prefix, request.getEmail());
                return false;
            }

            // Send OTP for password reset
            if (otpUseCase.sendPasswordResetOtp(user)) {
                log.info("{}|Password reset OTP sent successfully for email: {}", prefix, request.getEmail());
                return true;
            }

            log.error("{}|Failed to send password reset OTP for email: {}", prefix, request.getEmail());
            return false;
        } catch (Exception e) {
            log.error("{}|Exception sending password reset OTP: {}", prefix, e.getMessage(), e);
            return false;
        }
    }

    public boolean confirmPasswordReset(PasswordResetConfirmRequest request) {
        String prefix = "[PasswordResetUseCase]|confirmPasswordReset";
        log.info("{}|Password reset confirmation for email: {}", prefix, request.getEmail());
        try {
            // Validate inputs
            if (StringUtils.isBlank(request.getEmail()) || StringUtils.isBlank(request.getOtp()) 
                    || StringUtils.isBlank(request.getNewPassword())) {
                log.error("{}|Missing required fields", prefix);
                return false;
            }

            if (!CommonUtils.isEmail(request.getEmail())) {
                log.error("{}|Invalid email format: {}", prefix, request.getEmail());
                return false;
            }

            // Verify OTP
            OtpVerifyRequest otpRequest = new OtpVerifyRequest();
            otpRequest.setEmail(request.getEmail());
            otpRequest.setOtp(request.getOtp());

            String emailKey = request.getEmail().trim().toLowerCase();
            String key = String.format(Constant.RedisKey.OTP_EMAIL, emailKey);
            String cachedOtp = redisService.getRedisStringTemplate().opsForValue().get(key);

            if (StringUtils.isEmpty(cachedOtp) || !request.getOtp().equals(cachedOtp)) {
                log.error("{}|Invalid or expired OTP for email: {}", prefix, request.getEmail());
                return false;
            }

            // Get user
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.error("{}|User not found with email: {}", prefix, request.getEmail());
                return false;
            }

            User user = userOptional.get();
            
            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Clear OTP from Redis
            redisService.getRedisStringTemplate().delete(key);

            // Clear user cache to force refresh
            String userCacheKey = Constant.RedisKey.USER_ID + user.getId();
            redisService.getRedisStringTemplate().delete(userCacheKey);
            redisService.getRedisStringTemplate().delete(Constant.RedisKey.ALL_USERS);

            log.info("{}|Password reset successful for email: {}", prefix, request.getEmail());
            return true;
        } catch (Exception e) {
            log.error("{}|Exception confirming password reset: {}", prefix, e.getMessage(), e);
            return false;
        }
    }
}

