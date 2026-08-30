package com.raulbolivar.servicename.exception;

public class UnrecognizedClientException extends RuntimeException {
    public UnrecognizedClientException(String message) {
        super(message);
    }
}
