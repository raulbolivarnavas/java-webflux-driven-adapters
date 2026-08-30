package com.raulbolivar.lib.client.config;

import com.raulbolivar.lib.client.dto.HttpVerb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("adapters.generic-api-client")
public record GenericApiClientProperties(
        @NotEmpty Map<String, @Valid Endpoint> endpoints,
        Map<String, String> defaultHeaders,
        @Valid @NotNull Transport transport
) {
    public record Endpoint(
            @NotBlank String url,
            @NotNull HttpVerb method,
            Map<String, String> headers
    ) { }

    public record Transport(
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout,
            @NotNull Duration writeTimeout,
            @Min(1024) int maxInMemorySize
    ) { }
}
