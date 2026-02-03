package com.example.fakepaymentprovider.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    public void recordAuthentication(boolean success) {
        Counter counter = meterRegistry.counter("authentications_total", "status", success ? "success" : "fail");
        counter.increment();
    }

    public void recordWebhook(boolean success) {
        Counter counter = meterRegistry.counter("webhooks_total", "status", success ? "success" : "fail");
        counter.increment();
    }

    public void recordWebhookBucket(boolean success) {
        Counter counter = meterRegistry.counter("webhook_accepted", "status", success ? "consumed" : "rejected");
        counter.increment();
    }

    public void recordMerchantApiBucket(boolean success) {
        Counter counter = meterRegistry.counter("merchant_api_calls_total", "status", success ? "consumed" : "rejected");
        counter.increment();
    }
}
