package com.raulbolivar.servicename.model;

import java.util.Map;

public record StoredProcedureCommand(
        Map<String, Object> parameters
) {
}
