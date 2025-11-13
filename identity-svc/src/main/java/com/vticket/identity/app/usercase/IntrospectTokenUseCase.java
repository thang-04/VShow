package com.vticket.identity.app.usercase;

import com.vticket.identity.app.dto.req.IntrospectRequest;
import com.vticket.identity.app.dto.res.IntrospectResponse;
import com.vticket.identity.infra.jwt.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class IntrospectTokenUseCase {

    private final JwtService jwtService;

    public IntrospectResponse execute(IntrospectRequest request) {
        try {
            if (!jwtService.validateToken(request.getToken())) {
                return IntrospectResponse.builder()
                        .valid(false)
                        .build();
            }

            Claims claims = jwtService.parseToken(request.getToken());
            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();

            return IntrospectResponse.builder()
                    .valid(true)
                    .userId(claims.getId())
                    .username(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .issuedAt(issuedAt != null ? issuedAt.toInstant() : null)
                    .expiresAt(expiration != null ? expiration.toInstant() : null)
                    .build();
        } catch (Exception e) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }
}

