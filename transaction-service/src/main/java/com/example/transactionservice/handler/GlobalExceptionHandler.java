package com.example.transactionservice.handler;

import com.example.transaction.dto.ErrorResponse;
import com.example.transactionservice.exception.BadRequest;
import com.example.transactionservice.exception.NotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequest.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(BadRequest exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(400);
        response.setError(exception.getMessage());
        return ResponseEntity.status(400).body(response);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(ExpiredJwtException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(400);
        response.setError(exception.getMessage());
        return ResponseEntity.status(400).body(response);
    }
}
