package com.vticket.identity.infra.jwt;

import com.vticket.identity.domain.entity.User;
import com.vticket.identity.infra.config.KeyRegistry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final KeyRegistry keyRegistry;
    private final RSAService rsaService;

    public String generateAccessToken(User user) {
        return generateTokenRS256(user, keyRegistry.getAccessTokenTtlSeconds());
    }

    public String generateRefreshToken(User user) {
        return generateTokenRS256(user, (long) keyRegistry.getAccessTokenTtlSeconds() * 24 * 60);
    }

    public String generateTokenRS256(User user, long expirationMinutes) {
        //get active kid
        var active = keyRegistry.getKeys().stream()
                .filter(k -> k.getKid().equals(keyRegistry.getActiveKid()))
                .findFirst().orElseThrow();
        //get private key
        RSAPrivateKey privateKey = rsaService.loadPrivate(active.getPrivatePem());
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        String uuid = UUID.randomUUID().toString();
        return Jwts.builder()
                .header().add("kid", active.getKid()).and()
                .issuer(keyRegistry.getIssuer())
                .subject(user.getUsername())
                .id(user.getId())
                .claim("uuid", uuid)
                .claim("email",user.getEmail())
                .claim("roles",user.getRoles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Claims parseTokenRS256(String token) {
        //get active kid
        var active = keyRegistry.getKeys().stream()
                .filter(k -> k.getKid().equals(keyRegistry.getActiveKid()))
                .findFirst().orElseThrow();
        //get public key
        RSAPublicKey key = rsaService.loadPublicKey(active.getPublicPem());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String generateTokenHS256(User user, long expirationMinutes) {
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

    public Claims parseTokenHS256(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSignerKey().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseTokenRS256(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = parseTokenRS256(token);
        return claims.getId();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseTokenRS256(token);
        return claims.getSubject();
    }
}

