package com.disney.model.response;

import org.springframework.http.HttpStatus;

import java.util.List;


public class ApiErrorResponse {

    private HttpStatus status;

    private String message;

    private List<String> errors;
}
