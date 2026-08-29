package com.raulbolivar.servicename.model;

import java.util.List;

public record StoredProcedureResult(
        List<StoredProcedureResultSet> resultSets
) {
    public boolean isEmpty() {
        return resultSets == null || resultSets.isEmpty();
    }
}
