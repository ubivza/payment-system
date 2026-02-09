package com.example.currencyrateservice.integration;

import com.example.currencyrateservice.integration.dto.CBRConversionRateResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.micrometer.annotation.Timer;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CBRClientWrapper {
    private final CBRClient cbrClient;

    @CircuitBreaker(name = "default")
    @Retry(name = "default")
    @RateLimiter(name = "default")
    @Timer(name = "default")
    public CBRConversionRateResponse getRates(String date) {
        return cbrClient.getRates(date);
    }
}
