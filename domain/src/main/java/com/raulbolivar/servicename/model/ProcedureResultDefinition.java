package com.raulbolivar.servicename.model;

public record ProcedureResultDefinition(
        String codeField,
        String messageField,
        String returnField,
        String successCode
) {
}
