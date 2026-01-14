package com.vticket.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * - @SpringBootApplication: Đánh dấu đây là Spring Boot application
 * - scanBasePackages: Chỉ scan các package trong "com.vticket.notify"
 */
@SpringBootApplication()
@EnableDiscoveryClient
public class NotifySvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifySvcApplication.class, args);
    }

}
