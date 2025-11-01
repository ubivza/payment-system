package com.example.personservice.handler;

import com.example.personservice.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.example.person.dto.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(NotFoundException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(404);
        response.setError(exception.getMessage());
        return ResponseEntity.status(404).body(response);
    }
}
