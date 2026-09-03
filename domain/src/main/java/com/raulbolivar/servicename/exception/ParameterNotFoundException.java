package com.raulbolivar.servicename.exception;

public class ParameterNotFoundException extends RuntimeException {

    private final String parameterName;

    public ParameterNotFoundException(
            String parameterName,
            Throwable cause) {

        super(
                "No se encontró el parámetro solicitado: "
                        + parameterName,
                cause
        );

        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}