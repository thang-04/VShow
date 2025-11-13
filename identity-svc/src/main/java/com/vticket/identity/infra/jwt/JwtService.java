package com.vticket.identity.infra.jwt;

import com.vticket.identity.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        return generateToken(user, jwtProperties.getAccessTokenExpirationMinutes());
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, (long) jwtProperties.getRefreshTokenExpirationDays() * 24 * 60);
    }

    private String generateToken(User user, long expirationMinutes) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSignerKey().getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .id(user.getId())
                .subject(user.getUsername())
                .claim("email", user.getEmail())
                .claim("deviceId", user.getDeviceId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSignerKey().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
}

