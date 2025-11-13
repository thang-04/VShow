package com.vticket.gateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Mỗi request được gán một ID duy nhất (Correlation ID) ngay từ đầu.
 * ID này được truyền qua tất cả các service, giúp:
 * - Theo dõi request qua toàn bộ hệ thống
 * - Tìm tất cả logs liên quan đến một request cụ thể
 * - Debug dễ dàng hơn khi có lỗi
 *
 * CÁCH HOẠT ĐỘNG:
 * 1. Client gửi request (có thể có hoặc không có X-Correlation-ID header)
 * 2. Filter này kiểm tra:
 * - Nếu có header X-Correlation-ID → dùng ID đó
 * - Nếu không có → tạo ID mới (UUID)
 * 3. Thêm ID vào:
 * - Request header → truyền đến các service downstream
 * - Response header → trả về cho client
 * - MDC (Mapped Diagnostic Context) → tự động thêm vào mọi log
 *
 * VÍ DỤ:
 * Request: POST /api/identity/auth/login
 * Correlation ID: abc-123-def-456
 *
 * Logs sẽ có dạng:
 * [abc-123-def-456] Incoming request - Method: POST, Path: /api/identity/auth/login
 * [abc-123-def-456] Calling identity-svc...
 * [abc-123-def-456] Response received from identity-svc
 *
 * Khi có lỗi, chỉ cần search "abc-123-def-456" là tìm được tất cả logs liên quan
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    // Key trong MDC để lưu Correlation ID (dùng cho logging)
    private static final String CORRELATION_ID = "correlationId";

    /**
     * @param exchange ServerWebExchange - Chứa req và res
     * @param chain    GatewayFilterChain - Chuỗi các filter tiếp theo
     * @return Mono<Void> - Reactive programming (Spring WebFlux)
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // lấy Correlation ID từ request header
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // MDC tự động thêm ID này vào mọi log message
        MDC.put(CORRELATION_ID, correlationId);

        // add Correlation ID vào response header
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        // Thêm Correlation ID vào request header để truyền đến các service downstream
        // Các service khác sẽ nhận được ID này và tiếp tục truyền đi
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header(CORRELATION_ID_HEADER, correlationId)
                        .build())
                .build();
        return chain.filter(modifiedExchange)
                .doFinally(signalType -> MDC.clear());
    }

    /**
     * Thứ tự thực thi filter (Order)
     * HIGHEST_PRECEDENCE = Chạy đầu tiên (ưu tiên cao nhất)
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

