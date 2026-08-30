package com.raulbolivar.servicename.exception;

public final class OperationNotConfiguredException extends GenericApiClientException {
    public OperationNotConfiguredException(String operation) {
        super(operation, "Operación no configurada: " + operation, null);
    }
}
