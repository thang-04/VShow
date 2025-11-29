package com.vticket.identity.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        // chỉ định tên base cho file resources.
        source.setBasename("properties/i18n/messages"); // resources/properties/i18n/messages_vi.properties
        // encoding UTF-8
        source.setDefaultEncoding("UTF-8");
        // không dùng locale của hệ thống
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
