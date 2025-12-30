package com.example.api.kafka;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Jacksonized
public class WithdrawalRequestedEvent {
    private UUID transactionId;
    private UUID userId;
    private UUID walletId;
    private BigDecimal amount;
    private String currency;
    private String destination;
    private Instant timestamp;
}
