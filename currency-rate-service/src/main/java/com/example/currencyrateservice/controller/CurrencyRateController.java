package com.example.currencyrateservice.controller;

import com.example.currency.api.CurrencyRateApi;
import com.example.currency.dto.CurrencyResponse;
import com.example.currency.dto.RateProviderResponse;
import com.example.currency.dto.RateResponse;
import com.example.currencyrateservice.service.ConversionRateService;
import com.example.currencyrateservice.service.CurrencyService;
import com.example.currencyrateservice.service.RateProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CurrencyRateController implements CurrencyRateApi {
    private final ConversionRateService conversionRateService;
    private final CurrencyService currencyService;
    private final RateProviderService rateProviderService;

    @Override
    public ResponseEntity<List<CurrencyResponse>> getCurrencies(String authorization) {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @Override
    public ResponseEntity<RateResponse> getRate(String authorization, String from, String to, OffsetDateTime timestamp) {
        return ResponseEntity.ok(conversionRateService.getRateFromToForTime(from, to, timestamp));
    }

    @Override
    public ResponseEntity<List<RateProviderResponse>> getRateProviders(String authorization) {
        return ResponseEntity.ok(rateProviderService.getAllRateProviders());
    }
}
