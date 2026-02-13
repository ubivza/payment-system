package com.example.paymentservice.handler;

import com.example.payment.dto.ErrorResponse;
import com.example.paymentservice.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ErrorResponse handleException(NotFoundException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setError(exception.getMessage());
        response.setStatus(404);
        return response;
    }
}
