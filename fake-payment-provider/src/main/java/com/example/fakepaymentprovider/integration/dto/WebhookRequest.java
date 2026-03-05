package com.example.fakepaymentprovider.integration.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WebhookRequest(
        String type,
        UUID id,
        String status,
        String reason,
        String provider,
        BigDecimal amount
) {
}
