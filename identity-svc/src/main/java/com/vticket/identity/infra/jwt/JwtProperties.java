package com.vticket.identity.infra.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class JwtProperties {
    @Value("${jwt.signerKey}")
    private String signerKey;
    @Value("${jwt.accessTokenExpirationMinutes}")
    private int accessTokenExpirationMinutes;
    @Value("${jwt.refreshTokenExpirationDays}")
    private int refreshTokenExpirationDays;
}

