package com.vticket.identity.app.usercase;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.identity.app.dto.req.RefreshTokenRequest;
import com.vticket.identity.app.dto.res.TokenResponse;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.jwt.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public TokenResponse execute(RefreshTokenRequest request) {
        String prefix = "[RefreshTokenUseCase]";
        log.info(prefix);
        try {
            if (!jwtService.validateToken(request.getRefreshToken())) {
                throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            Claims claims = jwtService.parseTokenRS256(request.getRefreshToken());
            String userId = claims.getId();
            log.info("{}|userId={}", prefix, userId);
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                log.error("{}|userId={} not found", prefix, userId);
                return null;
            } else {
                User u = user.get();
                if (!u.getRefreshToken().equals(request.getRefreshToken())) {
                    log.error("{}|refresh token invalid", prefix);
                    return null;
                }
                //refresh new token
                String newAccessToken = jwtService.generateAccessToken(u);
                String newRefreshToken = jwtService.generateRefreshToken(u);

                u.updateTokens(newAccessToken, newRefreshToken);
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
}

