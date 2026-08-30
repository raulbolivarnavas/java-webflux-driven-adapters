package com.raulbolivar.servicename.exception;

public abstract class GenericApiClientException extends RuntimeException {
    private final String operation;

    protected GenericApiClientException(String operation, String message, Throwable cause) {
        super(message, cause);
        this.operation = operation;
    }

    public String operation() { return operation; }
}
