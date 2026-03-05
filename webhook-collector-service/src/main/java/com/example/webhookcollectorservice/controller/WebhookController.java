package com.example.webhookcollectorservice.controller;

import com.example.webhook.api.WebhookApi;
import com.example.webhook.dto.StatusUpdate;
import com.example.webhookcollectorservice.service.HMACAuthService;
import com.example.webhookcollectorservice.service.strategy.OperationTypeStrategyResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookController implements WebhookApi {
    private final HMACAuthService authService;
    private final OperationTypeStrategyResolver operationTypeStrategyResolver;

    @Override
    public ResponseEntity<Void> processWebhook(String xSignature, StatusUpdate statusUpdate) {
        if (!authService.authenticate(xSignature, statusUpdate)) {
            return ResponseEntity.status(401).build();
        }

        operationTypeStrategyResolver.resolve(statusUpdate.getType())
                .process(statusUpdate);
        return ResponseEntity.ok().build();
    }
}
