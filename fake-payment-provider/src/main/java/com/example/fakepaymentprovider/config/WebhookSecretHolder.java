package com.example.fakepaymentprovider.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "webhook")
public class WebhookSecretHolder {
    private String secretKey;
    private String outgoingUrl;
    private Scheduler scheduler = new Scheduler();

    @Setter
    @Getter
    public static class Scheduler {
        private long fixedDelayMs = 5000;
        private int batchSize = 100;
    }
}
