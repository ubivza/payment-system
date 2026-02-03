package com.example.fakepaymentprovider.handler;

import com.example.fake.dto.ErrorResponse;
import com.example.fakepaymentprovider.exception.AuthenticationException;
import com.example.fakepaymentprovider.exception.NotFoundException;
import com.example.fakepaymentprovider.exception.NotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final String AUTH_ERROR = "AUTH_ERROR";
    private final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private final String NOT_FOUND = "NOT_FOUND";

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ErrorResponse handleException(AuthenticationException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setError(AUTH_ERROR);
        response.setMessage(exception.getMessage());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotValidException.class)
    @ResponseBody
    public ErrorResponse handleException(NotValidException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setError(VALIDATION_ERROR);
        response.setMessage(exception.getMessage());
        return response;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ErrorResponse handleException(NotFoundException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setError(NOT_FOUND);
        response.setMessage(exception.getMessage());
        return response;
    }
}
