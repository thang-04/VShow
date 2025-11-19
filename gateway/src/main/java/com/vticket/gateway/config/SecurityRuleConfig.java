package com.vticket.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security.jwt")
@Data
public class SecurityRuleConfig {
    private List<String> public_api;
    private List<String> protected_api;
}


