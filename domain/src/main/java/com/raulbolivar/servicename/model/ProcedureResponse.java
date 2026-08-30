package com.raulbolivar.servicename.model;

import java.util.List;
import java.util.Map;

public record ProcedureResponse(
        List<Map<String, Object>> data,
        ProcedureStatus status
) {
}
