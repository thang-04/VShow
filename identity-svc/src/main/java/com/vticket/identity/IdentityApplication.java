package com.vticket.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * KIẾN TRÚC:
 * - Domain Layer: Business logic và entities (User, Role)
 * - Application Layer: Use cases (LoginUseCase, RegisterUseCase...)
 * - Infrastructure Layer: 
 *    - JPA: Tương tác với database (MySQL)
 *    - JWT: Tạo và verify JWT tokens
 * - Web Layer: REST Controllers (AuthController, TokenController)
 * ANNOTATIONS:
 * - @SpringBootApplication: Đánh dấu đây là Spring Boot application
 * - scanBasePackages: Chỉ scan các package trong "com.vticket.identity"
 * - @EnableJpaRepositories: Kích hoạt JPA repositories
 * - @EntityScan: Chỉ định package chứa JPA entities
 */
@SpringBootApplication(scanBasePackages = "com.vticket.identity")
@EnableJpaRepositories(basePackages = "com.vticket.identity.infra.jpa")
@EntityScan(basePackages = "com.vticket.identity.infra.jpa")
@EnableDiscoveryClient
public class IdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }

}
