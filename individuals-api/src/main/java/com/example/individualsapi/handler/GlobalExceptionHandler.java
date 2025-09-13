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

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotValidException.class)
    @ResponseBody
    public ErrorResponse handleException(NotValidException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(400);
        response.setError(exception.getMessage());
        return response;
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseBody
    public ErrorResponse handleException(UserAlreadyExistsException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(409);
        response.setError(exception.getMessage());
        return response;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseBody
    public ErrorResponse handleException(BadCredentialsException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(401);
        response.setError(exception.getMessage());
        return response;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ErrorResponse handleException(NotFoundException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(404);
        response.setError(exception.getMessage());
        return response;
    }
}
