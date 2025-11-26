//package com.vticket.notify.infra.mail;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
//
//@Configuration
//public class TemplateEngineConfig {
//
//    @Bean
//    public TemplateEngine templateEngine() {
//        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
//        resolver.setPrefix("/templates/email/");
//        resolver.setSuffix(".html");
//        resolver.setTemplateMode("HTML");
//        resolver.setCharacterEncoding("UTF-8");
//
//        TemplateEngine engine = new TemplateEngine();
//        engine.setTemplateResolver(resolver);
//        return engine;
//    }
//}
