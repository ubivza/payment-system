package com.example.currencyrateservice.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterLogger {

    private final RateLimiterRegistry rateLimiterRegistry;

    @PostConstruct
    void init() {
        rateLimiterRegistry
                .rateLimiter("getRatesRateLimiter")
                .getEventPublisher()
                .onFailure(event ->
                        log.warn("Rate limit exceeded"))
                .onSuccess(event ->
                        log.debug("Rate limit success"));
    }
}