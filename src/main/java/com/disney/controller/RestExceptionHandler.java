package com.disney.controller;

import com.disney.model.response.ApiErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

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
                NOT_FOUND,
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

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                BAD_REQUEST,
                ex.getMessage(),
                Collections.singletonList("Param not found")
        );
        return handleExceptionInternal(ex, apiErrorResponse, headers, apiErrorResponse.getStatus(), request);
    }

    @ExceptionHandler(value = {NoSuchElementException.class})
    protected ResponseEntity<Object> handleNoSuchElement(NoSuchElementException ex, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                NOT_FOUND,
                ex.getMessage(),
                Collections.singletonList("Non response was found")
        );
        return handleExceptionInternal(ex, apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                BAD_REQUEST,
                ex.getMessage(),
                Collections.singletonList("Malformed JSON request")
        );
        return handleExceptionInternal(ex, apiErrorResponse, headers, apiErrorResponse.getStatus(), request);
    }

    @ExceptionHandler(value = {DateTimeException.class, DateTimeParseException.class})
    protected ResponseEntity<Object> handleDateTimeParse(DateTimeException ex, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                BAD_REQUEST,
                ex.getMessage(),
                Collections.singletonList("Malformed date request")
        );
        return handleExceptionInternal(ex, apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus(), request);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                CONFLICT,
                ex.getMessage(),
                Collections.singletonList("Duplicate object")
        );
        return handleExceptionInternal(ex, apiErrorResponse, new HttpHeaders(), apiErrorResponse.getStatus(), request);
    }
}
