package com.raulbolivar.servicename.driven.spexecutor.dto;

import java.util.List;

public record StoredProcedureResultDto(
        List<StoredProcedureResultSetDto> resultSets
) {
    public boolean isEmpty() {
        return resultSets == null || resultSets.isEmpty();
    }
}
