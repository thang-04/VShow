package com.vticket.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
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
