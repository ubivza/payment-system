package com.example.individualsapi.service.api;

import com.example.currency.dto.RateResponse;
import reactor.core.publisher.Mono;

public interface CurrencyRateService {
    Mono<RateResponse> getActualRates(String from, String to);
}
