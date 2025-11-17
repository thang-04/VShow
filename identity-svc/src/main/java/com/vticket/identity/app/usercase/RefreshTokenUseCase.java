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
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public TokenResponse execute(RefreshTokenRequest request) {
        if (!jwtService.validateToken(request.getRefreshToken())) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Claims claims = jwtService.parseTokenRS256(request.getRefreshToken());
        String userId = claims.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getRefreshToken().equals(request.getRefreshToken())) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        user.updateTokens(newAccessToken, newRefreshToken);
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(ChronoUnit.SECONDS.between(Instant.now(), 
                        Instant.now().plus(60, ChronoUnit.MINUTES)))
                .build();
    }
}

