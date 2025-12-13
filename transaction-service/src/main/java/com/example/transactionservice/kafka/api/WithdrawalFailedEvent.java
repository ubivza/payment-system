package com.example.transactionservice.kafka.api;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Jacksonized
public class WithdrawalFailedEvent {
    private UUID transactionId;
    private String status;
    private String failureReason;
    private Instant timestamp;
}
