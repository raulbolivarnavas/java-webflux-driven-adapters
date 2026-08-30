package com.raulbolivar.lib.resilience.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("adapters.generic-api-client")
public record GenericApiResilienceProperties(
        @Valid @NotNull Resilience resilience
) {

    public record Resilience(
            @Valid @NotNull CircuitBreaker circuitBreaker,
            @Valid @NotNull Retry retry,
            @NotNull Duration timeout
    ) {

        public record CircuitBreaker(
                float failureRateThreshold,
                float slowCallRateThreshold,
                @NotNull Duration slowCallDurationThreshold,
                @Min(1) int slidingWindowSize,
                @Min(1) int minimumNumberOfCalls,
                @NotNull Duration waitDurationInOpenState,
                @Min(1) int permittedCallsInHalfOpenState
        ) { }

        public record Retry(
                @Min(1) int maxAttempts,
                @NotNull Duration waitDuration
        ) { }
    }
}
