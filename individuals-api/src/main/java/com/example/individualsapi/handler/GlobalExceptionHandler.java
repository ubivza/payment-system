package com.example.individualsapi.handler;

import com.example.dto.ErrorResponse;
import com.example.individualsapi.exception.BadCredentialsException;
import com.example.individualsapi.exception.NotFoundException;
import com.example.individualsapi.exception.NotValidException;
import com.example.individualsapi.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotValidException.class)
    @ResponseBody
    public Mono<ErrorResponse> handleException(NotValidException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(400);
        response.setError(exception.getMessage());
        return Mono.just(response);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseBody
    public Mono<ErrorResponse> handleException(UserAlreadyExistsException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(409);
        response.setError(exception.getMessage());
        return Mono.just(response);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseBody
    public Mono<ErrorResponse> handleException(BadCredentialsException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(401);
        response.setError(exception.getMessage());
        return Mono.just(response);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public Mono<ErrorResponse> handleException(NotFoundException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(404);
        response.setError(exception.getMessage());
        return Mono.just(response);
    }
}
