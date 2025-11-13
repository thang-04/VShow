package com.vticket.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    /**
     * Định nghĩa các routes cho Gateway
     *
     * @param builder RouteLocatorBuilder - Builder để tạo routes
     * @return RouteLocator - Chứa danh sách các routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Vd: /api/identity/auth/login -> chuyển đến identity-svc
                .route("identity-service", r -> r
                        .path("/api/identity/**")
                        .uri("lb://identity-svc"))  // lb:// = Load Balancer, tự động tìm service qua Eureka

                // Vd: /api/events/list -> chuyển đến event-catalog-svc
                .route("event-catalog-service", r -> r
                        .path("/api/events/**")
                        .uri("lb://event-catalog-svc"))  // Tự động load balance giữa các instance

                .build();
    }
}

