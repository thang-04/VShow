package com.vticket.gateway.filter;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class JwtValidator {

    @Value("${jwt.jwks-url}")
    private String jwksUrl;

    @Value("${jwt.issuer}")
    private String issuer;

    private NimbusReactiveJwtDecoder decoder;
    private Instant lastInit = Instant.MIN;

    /**
     * Rebuild decoder mỗi 5 phút or khi decode lỗi or có key mới
     */
    private synchronized void initDecoder(boolean force) {
        if (decoder == null || force ||
                Duration.between(lastInit, Instant.now()).toMinutes() >= 5) {
            decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwksUrl).build();
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
            lastInit = Instant.now();
        }
    }

    public Mono<Jwt> validate(String token) {
        initDecoder(false);
        return decoder.decode(token)
                .onErrorResume(err -> {
                    // if identity-svc rotate new key → refresh
                    initDecoder(true);
                    return decoder.decode(token);
                });
    }
}

