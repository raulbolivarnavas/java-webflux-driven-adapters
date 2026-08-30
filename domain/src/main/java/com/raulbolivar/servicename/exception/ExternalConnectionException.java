package com.raulbolivar.servicename.exception;

public final class ExternalConnectionException extends GenericApiClientException {
    public ExternalConnectionException(String operation, Throwable cause) {
        super(operation, "No fue posible conectar con el backend", cause);
    }
}
