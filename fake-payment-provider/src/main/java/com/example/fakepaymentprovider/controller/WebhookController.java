package com.example.fakepaymentprovider.controller;

import com.example.fake.api.WebhookApi;
import com.example.fake.dto.StatusUpdate;
import com.example.fakepaymentprovider.service.HMACAuthService;
import com.example.fakepaymentprovider.service.WebhookService;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController implements WebhookApi {
    private final WebhookService webhookService;
    private final HMACAuthService authService;
    private final Bucket bucket;

    public WebhookController(WebhookService webhookService, HMACAuthService authService, @Qualifier("webhookBucket") Bucket bucket) {
        this.webhookService = webhookService;
        this.authService = authService;
        this.bucket = bucket;
    }

    @Override
    public ResponseEntity<Void> updatePayout(String xSignature, StatusUpdate statusUpdate) {
        if (!authService.authenticate(xSignature, statusUpdate)) {
            return ResponseEntity.status(401).build();
        }

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        webhookService.updatePayout(statusUpdate);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updateTransaction(String xSignature, StatusUpdate statusUpdate) {
        if (!authService.authenticate(xSignature, statusUpdate)) {
            return ResponseEntity.status(401).build();
        }

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        webhookService.updateTransaction(statusUpdate);
        return ResponseEntity.ok().build();
    }
}
