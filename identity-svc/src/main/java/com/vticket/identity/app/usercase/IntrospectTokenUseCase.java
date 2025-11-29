package com.vticket.identity.app.usercase;

import com.vticket.identity.app.dto.req.IntrospectRequest;
import com.vticket.identity.app.dto.res.IntrospectResponse;
import com.vticket.identity.infra.jwt.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntrospectTokenUseCase {

    private final JwtService jwtService;

    public IntrospectResponse execute(IntrospectRequest request) {
        String prefix = "[IntrospectTokenUseCase]|request=" + gson.toJson(request);
        log.info(prefix);
        try {
            if (!jwtService.validateToken(request.getToken())) {
                return IntrospectResponse.builder()
                        .valid(false)
                        .build();
            }

            Claims claims = jwtService.parseTokenRS256(request.getToken());
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
            log.error("{}|Exception={}", prefix, e.getMessage());
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }
}

