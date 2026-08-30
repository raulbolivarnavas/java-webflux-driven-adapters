package com.raulbolivar.servicename.exception;

public class SecretsAccessDeniedException extends RuntimeException {

    public SecretsAccessDeniedException(String message) {
        super(message);
    }

    public SecretsAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
