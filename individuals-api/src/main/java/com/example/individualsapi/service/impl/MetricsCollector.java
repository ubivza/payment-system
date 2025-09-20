package com.example.individualsapi.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    public void recordRegistration(boolean success) {
        Counter counter = meterRegistry.counter("registrations_total", "status", success ? "success" : "fail");
        counter.increment();
    }

    public void recordLogin(boolean success) {
        Counter counter = meterRegistry.counter("logins_total", "status", success ? "success" : "fail");
        counter.increment();
    }
}
