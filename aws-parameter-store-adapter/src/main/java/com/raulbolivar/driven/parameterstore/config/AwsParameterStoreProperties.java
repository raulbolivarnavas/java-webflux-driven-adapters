package com.raulbolivar.driven.parameterstore.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

@Validated
@ConfigurationProperties("adapters.aws-parameter-store")
public record AwsParameterStoreProperties(
        @NotBlank String region,
        URI endpointOverride,
        @NotNull Duration apiCallTimeout,
        @NotNull Duration apiCallAttemptTimeout,
        @Valid @NotNull Cache cache
) {

    public record Cache(
            boolean enabled,

            @NotNull
            Duration ttl,

            @Min(1)
            int maximumSize
    ) {
    }
}