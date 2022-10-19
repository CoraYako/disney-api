package com.disney.controller;

import com.disney.model.response.ApiErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ':' + fieldError.getDefaultMessage())
                .collect(Collectors.toList());
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(status, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, apiErrorResponse, headers, apiErrorResponse.getStatus(), request);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                BAD_REQUEST,
                ex.getMessage(),
                Collections.singletonList("Param not found"));
        return handleExceptionInternal(ex, apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus(), request);
    }

    @ExceptionHandler(value = {EntityExistsException.class})
    protected ResponseEntity<Object> handleEntityExists(EntityExistsException ex, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                CONFLICT,
                ex.getMessage(),
                Collections.singletonList("Duplicated entity"));
        return handleExceptionInternal(ex, apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus(), request);
    }
}
