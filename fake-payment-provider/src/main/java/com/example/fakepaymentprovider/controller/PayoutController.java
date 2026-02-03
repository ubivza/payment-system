package com.example.fakepaymentprovider.controller;

import com.example.fake.api.PayoutApi;
import com.example.fake.dto.Payout;
import com.example.fake.dto.PayoutRequest;
import com.example.fakepaymentprovider.service.MerchantService;
import com.example.fakepaymentprovider.service.PayoutService;
import com.example.fakepaymentprovider.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PayoutController implements PayoutApi {
    private final PayoutService service;
    private final RateLimiterService limiterService;
    private final MerchantService merchantService;

    @Override
    public ResponseEntity<Payout> createPayout(PayoutRequest payoutRequest) {
        if (!limiterService.tryConsume(merchantService.getCurrentMerchantId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(service.create(payoutRequest));
    }

    @Override
    public ResponseEntity<Payout> getPayoutById(UUID id) {
        if (!limiterService.tryConsume(merchantService.getCurrentMerchantId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(service.getById(id));
    }

    @Override
    public ResponseEntity<List<Payout>> getPayouts(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (!limiterService.tryConsume(merchantService.getCurrentMerchantId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(service.getByPeriod(startDate, endDate));
    }
}
