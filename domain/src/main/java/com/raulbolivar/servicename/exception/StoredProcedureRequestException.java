package com.raulbolivar.servicename.exception;

public class StoredProcedureRequestException extends RuntimeException {

    public StoredProcedureRequestException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
