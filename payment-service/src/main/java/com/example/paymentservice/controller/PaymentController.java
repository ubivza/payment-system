package com.example.paymentservice.controller;

import com.example.payment.api.PaymentApi;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService service;

    @Override
    public ResponseEntity<PaymentResponse> createPayment(String authorization, PaymentRequest paymentRequest) {
        return ResponseEntity.status(201)
                .body(service.create(paymentRequest));
    }
}
