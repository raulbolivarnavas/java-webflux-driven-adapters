package com.raulbolivar.servicename.exception;

import lombok.Getter;

@Getter
public class StoredProcedureExecutionException extends RuntimeException {

    private final Integer errorCode;
    private final String sqlState;

    public StoredProcedureExecutionException(
            Integer errorCode,
            String sqlState,
            String message,
            Throwable cause) {

        super(message, cause);

        this.errorCode = errorCode;
        this.sqlState = sqlState;
    }
}
