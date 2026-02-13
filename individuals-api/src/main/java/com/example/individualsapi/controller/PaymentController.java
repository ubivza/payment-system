package com.example.individualsapi.controller;

import com.example.individuals.dto.PaymentMethodResponseDto;
import com.example.individualsapi.service.api.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("${individuals-api.path}/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService service;

    @GetMapping("/{currencyCode}/{countryCode}")
    public Flux<PaymentMethodResponseDto> getAvailablePaymentMethods(@PathVariable String currencyCode,
                                                                           @PathVariable String countryCode) {
        return service.getAvailable(currencyCode, countryCode);
    }
}
