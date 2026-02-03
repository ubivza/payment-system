package com.example.fakepaymentprovider.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Builder
@Getter
@Jacksonized
public class WebhookPayload {
    private UUID id;
    private String status;
    private String reason;
}
