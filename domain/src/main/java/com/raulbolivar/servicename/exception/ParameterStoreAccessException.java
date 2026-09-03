package com.raulbolivar.servicename.exception;

public class ParameterStoreAccessException
        extends RuntimeException {

    private final String parameterName;

    public ParameterStoreAccessException(
            String parameterName,
            String message,
            Throwable cause) {

        super(message, cause);
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}