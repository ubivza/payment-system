package com.example.webhookcollectorservice.handler;

import com.example.webhook.dto.ErrorResponse;
import com.example.webhookcollectorservice.exception.NotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(NotValidException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setMessage(exception.getMessage());
        return ResponseEntity.status(400).body(response);
    }
}
