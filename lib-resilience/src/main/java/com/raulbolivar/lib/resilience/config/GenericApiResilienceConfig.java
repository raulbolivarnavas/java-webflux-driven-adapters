package com.raulbolivar.lib.resilience.config;

import com.raulbolivar.lib.resilience.adapter.GenericApiResilienceExecutor;
import com.raulbolivar.servicename.exception.ExternalClientException;
import com.raulbolivar.servicename.exception.ExternalResponseMappingException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GenericApiResilienceProperties.class)
public class GenericApiResilienceConfig {

    @Bean
    GenericApiResilienceExecutor genericApiResilienceExecutor(GenericApiResilienceProperties properties) {

        var resilience = properties.resilience();
        var cb = resilience.circuitBreaker();
        var retry = resilience.retry();

        var circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(cb.failureRateThreshold())
                .slowCallRateThreshold(cb.slowCallRateThreshold())
                .slowCallDurationThreshold(cb.slowCallDurationThreshold())
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(cb.slidingWindowSize())
                .minimumNumberOfCalls(cb.minimumNumberOfCalls())
                .waitDurationInOpenState(cb.waitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(cb.permittedCallsInHalfOpenState())
                .ignoreExceptions(ExternalClientException.class, ExternalResponseMappingException.class)
                .build();

        var retryConfig = RetryConfig.custom()
                .maxAttempts(retry.maxAttempts())
                .waitDuration(retry.waitDuration())
                .retryOnException(GenericApiResilienceExecutor::retryable)
                .build();

        var timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(resilience.timeout())
                .cancelRunningFuture(true)
                .build();

        return new GenericApiResilienceExecutor(
                CircuitBreakerRegistry.of(circuitBreakerConfig),
                RetryRegistry.of(retryConfig),
                TimeLimiterRegistry.of(timeLimiterConfig));
    }
}
