package com.vticket.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 1. Client chỉ cần biết địa chỉ của Gateway (ví dụ: localhost:8080)
 * 2. Gateway nhận request, xác định service nào cần xử lý dựa trên URL path
 * 3. Gateway chuyển tiếp (route) request đến service tương ứng
 * VÍ DỤ:
 * - Client gọi: POST http://localhost:8080/api/identity/auth/login
 * - Gateway nhận request, kiểm tra path "/api/identity/**"
 * - Gateway route đến service "identity-svc" (thông qua Eureka)
 * - Identity service xử lý và trả về response
 * - Gateway trả response về cho client
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

