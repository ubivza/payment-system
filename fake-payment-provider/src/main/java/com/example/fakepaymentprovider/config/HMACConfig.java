package com.example.fakepaymentprovider.config;

import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HMACConfig {
    @Bean
    public HmacUtils hmacUtils(WebhookSecretHolder secretHolder) {
        return new HmacUtils("HmacSHA256", secretHolder.getSecretKey());
    }
}
