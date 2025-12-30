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
public class DepositCompletedEvent {
    private UUID transactionId;
    private String status;
    private BigDecimal amount;
    private Instant timestamp;
}
