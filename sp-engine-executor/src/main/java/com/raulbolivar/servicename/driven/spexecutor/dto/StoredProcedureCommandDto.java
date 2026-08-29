package com.raulbolivar.servicename.driven.spexecutor.dto;

import java.util.Map;

public record StoredProcedureCommandDto(
        Map<String, Object> parameters
) {
}
