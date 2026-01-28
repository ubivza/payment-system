package com.example.individualsapi.exception;

public class InnerServiceException extends RuntimeException {
    public InnerServiceException(String message) {
        super(message);
    }
}
