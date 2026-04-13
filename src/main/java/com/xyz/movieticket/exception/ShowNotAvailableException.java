package com.xyz.movieticket.exception;

public class ShowNotAvailableException extends RuntimeException {
    public ShowNotAvailableException(String message) {
        super(message);
    }
}