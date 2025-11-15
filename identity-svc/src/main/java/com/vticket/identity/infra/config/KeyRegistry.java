package com.vticket.identity.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class KeyRegistry {

    private String issuer;
    private long accessTokenTtlSeconds;
    private String activeKid;
    private List<KeyProp> keys = new ArrayList<>();

    @Setter
    @Getter
    public static class KeyProp {
        private String kid;
        private String privatePem;
        private String publicPem;
    }
}
