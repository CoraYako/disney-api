package com.disney.model;

public class InvalidUUIDFormatException extends RuntimeException {

    public InvalidUUIDFormatException(String message) {
        super(message);
    }
}
