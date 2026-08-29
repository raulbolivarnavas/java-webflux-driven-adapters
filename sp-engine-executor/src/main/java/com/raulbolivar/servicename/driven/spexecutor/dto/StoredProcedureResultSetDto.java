package com.raulbolivar.servicename.driven.spexecutor.dto;

import java.util.List;
import java.util.Map;

public record StoredProcedureResultSetDto(
        int index,
        List<Map<String, Object>> rows
) {
}
