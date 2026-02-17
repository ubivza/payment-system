package com.example.paymentservice.controller;

import com.example.payment.api.PaymentMethodApi;
import com.example.payment.dto.PaymentMethodResponse;
import com.example.paymentservice.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentMethodController implements PaymentMethodApi {
    private final PaymentMethodService service;

    @Override
    public ResponseEntity<List<PaymentMethodResponse>> getPaymentMethods(String authorization, String currencyCode, String countryCode) {
        return ResponseEntity.ok(service.get(currencyCode, countryCode));
    }

    @Override
    public ResponseEntity<PaymentMethodResponse> getPaymentMethodById(String authorization, String methodId) {
        return ResponseEntity.ok(service.getById(UUID.fromString(methodId)));
    }
}
