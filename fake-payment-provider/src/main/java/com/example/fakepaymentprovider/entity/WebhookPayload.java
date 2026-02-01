package com.example.fakepaymentprovider.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
public class WebhookPayload {
    private Long id;
    private String status;
    private String reason;
}
