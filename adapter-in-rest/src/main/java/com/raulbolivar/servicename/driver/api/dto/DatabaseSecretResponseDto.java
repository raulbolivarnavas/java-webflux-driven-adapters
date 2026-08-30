package com.raulbolivar.servicename.driver.api.dto;

public record DatabaseSecretResponseDto(
        String username,
        String password,
        String host,
        String port,
        String database
) {
}
