package com.raulbolivar.driven.secretsmanager.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

@Validated
@ConfigurationProperties("adapters.aws-secrets-manager")
public record AwsSecretsManagerProperties(
        @NotBlank String region,
        URI endpointOverride,
        @NotNull Duration apiCallTimeout,
        @NotNull Duration apiCallAttemptTimeout,
        @NotNull Cache cache
) {

    public record Cache(
            boolean enabled,
            @NotNull Duration ttl
    ) {
    }
}
