package com.vticket.eventcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.vticket.eventcatalog")
@EnableJpaRepositories(basePackages = "com.vticket.eventcatalog.infra.jpa")
@EntityScan(basePackages = "com.vticket.eventcatalog.infra.jpa")
@EnableDiscoveryClient
public class EventCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventCatalogApplication.class, args);
    }

}
