package com.vticket.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Filter này ghi log cho tất cả request đi qua Gateway
 *
 * THÔNG TIN ĐƯỢC LOG:
 * 1. Khi request vào:
 * - HTTP Method (GET, POST, PUT, DELETE...)
 * - Path (URL path)
 * - Correlation ID (để trace request)
 *
 * 2. Khi response :
 * - HTTP Method
 * - Path
 * - Status Code (200, 404, 500...)
 * - Duration (milliseconds)
 * - Correlation ID
 *
 * VD LOG:
 * [INFO] Incoming request - Method: POST, Path: /api/identity/auth/login, Correlation-ID: abc-123
 * [INFO] Outgoing response - Method: POST, Path: /api/identity/auth/login, Status: 200, Duration: 150ms, Correlation-ID: abc-123
 */
@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    /**
     * @param exchange ServerWebExchange - Chứa req và res
     * @param chain    GatewayFilterChain - Chuỗi các filter
     * @return Mono<Void> - Reactive programming
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        Instant startTime = Instant.now();

        log.info("Incoming request - Method: {}, Path: {}, Correlation-ID: {}",
                method, path, correlationId);

        // then() = chạy sau khi chain.filter() done
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;
            String corrId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

            log.info("Outgoing response - Method: {}, Path: {}, Status: {}, Duration: {}ms, Correlation-ID: {}",
                    method, path, statusCode, duration, corrId != null ? corrId : correlationId);
        }));
    }

    /**
     * LOWEST_PRECEDENCE = Chạy cuối cùng
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

