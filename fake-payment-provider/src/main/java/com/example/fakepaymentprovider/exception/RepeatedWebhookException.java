package com.example.fakepaymentprovider.exception;

public class RepeatedWebhookException extends RuntimeException {
    public RepeatedWebhookException(String message) {
        super(message);
    }
}
