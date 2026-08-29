package com.raulbolivar.servicename.model;

import java.util.List;
import java.util.Map;

public record StoredProcedureResultSet(
        int index,
        List<Map<String, Object>> rows
) {
}
