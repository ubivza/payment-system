package com.example.webhookcollectorservice.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TransactionStatusType {
    DEPOSIT_SUCCESS("deposit", "COMPLETED"),
    WITHDRAWAL_SUCCESS("withdrawal", "COMPLETED"),
    WITHDRAWAL_FAILED("withdrawal", "FAILED"),
    UNKNOWN("unknown", "unknown");

    private final String type;
    private final String status;

    public static TransactionStatusType fromTypeAndStatus(String type, String status) {
        return Arrays.stream(values())
                .filter(t -> t.type.equals(type) && t.status.equals(status))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
