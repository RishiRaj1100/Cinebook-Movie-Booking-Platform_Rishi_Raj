package com.cinebook.exception;

public class SeatsUnavailableException extends RuntimeException {
    public SeatsUnavailableException(String message) {
        super(message);
    }
}
