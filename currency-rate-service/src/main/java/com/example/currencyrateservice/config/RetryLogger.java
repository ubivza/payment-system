package com.example.currencyrateservice.config;

import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryLogger {
    private final RetryRegistry retryRegistry;

    @PostConstruct
    void init() {
        retryRegistry
                .retry("getRatesRetry")
                .getEventPublisher()
                .onRetry(event ->
                        log.warn("Retry #{} due to {}",
                                event.getNumberOfRetryAttempts(),
                                event.getLastThrowable() != null
                                        ? event.getLastThrowable().getMessage()
                                        : "unknown"))
                .onError(event ->
                        log.error("Retry error", event.getLastThrowable()));
    }
}
