package com.example.fakepaymentprovider.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebhookHttpClientConfig {
    @Bean
    public RestTemplate webhookRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}