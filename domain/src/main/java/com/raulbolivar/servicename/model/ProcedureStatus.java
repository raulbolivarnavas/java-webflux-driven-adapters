package com.raulbolivar.servicename.model;

public record ProcedureStatus(
        boolean success,
        String code,
        String message
) {
}
