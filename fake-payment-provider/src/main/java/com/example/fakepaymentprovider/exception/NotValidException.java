package com.example.fakepaymentprovider.exception;

public class NotValidException extends RuntimeException {
    public NotValidException(String message) {
        super(message);
    }
}
