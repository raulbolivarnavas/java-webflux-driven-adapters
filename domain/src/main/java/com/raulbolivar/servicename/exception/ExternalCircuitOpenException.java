package com.raulbolivar.servicename.exception;

public final class ExternalCircuitOpenException extends GenericApiClientException {
    public ExternalCircuitOpenException(String operation, Throwable cause) {
        super(operation, "El circuito del backend está abierto", cause);
    }
}
