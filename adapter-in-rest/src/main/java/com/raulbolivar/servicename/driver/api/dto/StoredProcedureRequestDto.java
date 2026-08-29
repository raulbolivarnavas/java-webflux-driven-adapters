package com.raulbolivar.servicename.driver.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record StoredProcedureRequestDto(
        @NotNull
        Map<String, Object> parameters
) {
}
