package com.vticket.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CẤU TRÚC JWT:
 * Header.Payload.Signature
 * - Header: Loại token và thuật toán mã hóa
 * - Payload: Thông tin người dùng (claims)
 * - Signature: Chữ ký để verify token không bị giả mạo
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.signerKey:your-256-bit-secret-key-must-be-at-least-32-characters-long}")
    private String signerKey;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/identity/auth/login",
            "/api/identity/auth/register",
            "/api/identity/token/introspect"
    );

    /**
     * @param exchange ServerWebExchange - Chứa req và res
     * @param chain GatewayFilterChain - Chuỗi các filter
     * @return Mono<Void> - Reactive programming
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        //check if public path
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(signerKey.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)  // Parse token và verify signature
                    .getPayload();  // Lấy payload

            // Các service downstream có thể dùng thông tin này mà không cần parse token lại
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", claims.getId())        // User ID từ token
                    .header("X-Username", claims.getSubject())  // Username từ token
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "Invalid or expired token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * @param exchange ServerWebExchange
     * @param message Thông báo lỗi
     * @param status HTTP status code
     * @return Mono<Void>
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorBody = String.format(
                "{\"code\":%d,\"codeName\":\"%s\",\"desc\":\"%s\"}",
                status.value() == 401 ? 1006 : -9999,  // Error code
                status.value() == 401 ? "UNAUTHENTICATED" : "ERROR_CODE_EXCEPTION",  // Error name
                message  // Error message
        );
        
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8)))
        );
    }

    /**
     * HIGHEST_PRECEDENCE + 1 = Chạy sau CorrelationIdFilter (HIGHEST_PRECEDENCE)
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}

