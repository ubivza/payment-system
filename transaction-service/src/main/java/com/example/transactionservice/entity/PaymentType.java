package com.example.transactionservice.entity;

import com.example.transactionservice.exception.BadRequest;

import java.util.Arrays;

public enum PaymentType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER;

    public static PaymentType fromString(String type) {
        return Arrays.stream(PaymentType.values())
                .filter(typeFromEnum -> typeFromEnum.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new BadRequest(String.format("Type %s is not supported!", type)));
    }
}
