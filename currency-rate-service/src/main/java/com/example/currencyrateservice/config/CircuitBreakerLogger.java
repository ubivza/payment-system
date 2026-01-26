package com.example.currencyrateservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CircuitBreakerLogger {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    void init() {
        circuitBreakerRegistry
                .circuitBreaker("getRatesCircuitBreaker")
                .getEventPublisher()
                .onStateTransition(event ->
                        log.warn("CB transition: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onFailureRateExceeded(event ->
                        log.warn("CB failure rate exceeded: {}", event))
                .onCallNotPermitted(event ->
                        log.warn("CB call not permitted"))
                .onError(event ->
                        log.error("CB error", event.getThrowable()));
    }
}
