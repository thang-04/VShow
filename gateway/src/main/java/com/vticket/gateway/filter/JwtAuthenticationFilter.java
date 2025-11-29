package com.vticket.gateway.filter;

import com.vticket.gateway.config.SecurityRuleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * CẤU TRÚC JWT:
 * Header.Payload.Signature
 * - Header: Loại token và thuật toán mã hóa
 * - Payload: Thông tin người dùng (claims)
 * - Signature: Chữ ký để verify token không bị giả mạo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final String PREFIX = "[JwtAuthenticationFilter]|";

    private final SecurityRuleConfig securityRuleConfig;
    private final JwtValidator jwtValidator;
    private final AntPathMatcher matcher = new AntPathMatcher();

    private boolean isPublicPath(String method, String path) {
        return securityRuleConfig.getPublic_api().stream().anyMatch(rule -> {
            String[] parts = rule.split(":");
            return parts.length == 2
                    && parts[0].equalsIgnoreCase(method)
                    && matcher.match(parts[1], path);
        });
    }

    /**
     * @param exchange ServerWebExchange - Chứa req và res
     * @param chain    GatewayFilterChain - Chuỗi các filter
     * @return Mono<Void> - Reactive programming
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String logPrefix = "[filter]|";
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        log.info("{}Request path:{} {} ", PREFIX + logPrefix, method, path);
        //check if public path
        if (isPublicPath(method, path)) {
            log.info("{}|Public Api [{}: {}]", logPrefix, method, path);
            return chain.filter(exchange);
        }
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);

        return jwtValidator.validate(token)
                .flatMap(jwt -> {
                    //config info to service downstream
                    ServerHttpRequest modifiedReq = request.mutate()
                            .header("X-USER-ID", jwt.getClaimAsString("jti"))
                            .header("X-USERNAME", jwt.getClaimAsString("sub"))
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedReq).build());
                })
                .onErrorResume(err ->
                        onError(exchange, "Invalid or expired token: " + err.getMessage(), HttpStatus.UNAUTHORIZED)
                );

//        try {
//            SecretKey key = Keys.hmacShaKeyFor(signerKey.getBytes(StandardCharsets.UTF_8));
//
//            Claims claims = Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)  // Parse token và verify signature
//                    .getPayload();  // Lấy payload
//
//            // Các service downstream có thể dùng thông tin này mà không cần parse token lại
//            ServerHttpRequest modifiedRequest = request.mutate()
//                    .header("X-User-Id", claims.getId())        // User ID từ token
//                    .header("X-Username", claims.getSubject())  // Username từ token
//                    .build();
//
//            return chain.filter(exchange.mutate().request(modifiedRequest).build());
//        } catch (Exception e) {
//            return onError(exchange, "Invalid or expired token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
//        }
    }

    //    private boolean isPublicPath(String path) {
//        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
//    }

    /**
     * @param exchange ServerWebExchange
     * @param message  Thông báo lỗi
     * @param status   HTTP status code
     * @return Mono<Void>
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.error("[ERROR] {} - {}", status, message);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"code\":%d,\"codeName\":\"%s\",\"desc\":\"%s\"}",
                status.value() == 401 ? 1006 : -9999,  // Error code
                status.value() == 401 ? "UNAUTHENTICATED" : "ERROR_CODE_EXCEPTION",  // Error name
                message  // Error message
        );
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
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

