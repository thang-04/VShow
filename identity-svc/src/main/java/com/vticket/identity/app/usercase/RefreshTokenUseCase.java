package com.vticket.identity.app.usercase;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.identity.app.dto.req.RefreshTokenRequest;
import com.vticket.identity.app.dto.res.TokenResponse;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.jwt.JwtService;
import com.vticket.identity.infra.redis.RedisService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse execute(RefreshTokenRequest request) {
        String prefix = "[RefreshTokenUseCase]";
        log.info(prefix);
        try {
            if (!jwtService.validateToken(request.getRefreshToken())) {
                throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            Claims claims = validateAndParseClaims(request.getRefreshToken());
            String userId = claims.getId();
            log.info("{}|userId={}", prefix, userId);
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                log.error("{}|userId={} not found", prefix, userId);
                return null;
            } else {
                User u = user.get();
                if (!verifyRefreshToken(request.getRefreshToken(), u.getRefreshToken())) {
                    log.error("{}|Invalid refresh token for userId={}", prefix, userId);
                    return null;
                }

                //need-update: cache redis

                //gen new token
                String newAccessToken = jwtService.generateAccessToken(u);
                String newRefreshToken = jwtService.generateRefreshToken(u);
                String refreshTokenHash = passwordEncoder.encode(newRefreshToken);

                u.updateTokens(newAccessToken, refreshTokenHash);
                userRepository.save(u);
                return TokenResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .tokenType("Bearer")
                        .expiresIn(ChronoUnit.SECONDS.between(Instant.now(),
                                Instant.now().plus(60, ChronoUnit.MINUTES)))
                        .build();
            }
        } catch (Exception e) {
            log.error("{}|Exception={}", prefix, e.getMessage());
        }
        return null;
    }

    private Claims validateAndParseClaims(String token) {
        if (!jwtService.validateToken(token)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        Claims claims = jwtService.parseTokenRS256(token);
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new AppException(ErrorCode.INVALID_TOKEN_TYPE);
        }
        return claims;
    }

    private boolean verifyRefreshToken(String rawToken, String hashedToken) {
        return passwordEncoder.matches(rawToken, hashedToken);
    }
}

