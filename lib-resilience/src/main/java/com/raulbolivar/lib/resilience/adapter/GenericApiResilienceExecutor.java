package com.raulbolivar.lib.resilience.adapter;

import com.raulbolivar.servicename.exception.ExternalCircuitOpenException;
import com.raulbolivar.servicename.exception.ExternalClientException;
import com.raulbolivar.servicename.exception.ExternalResponseMappingException;
import com.raulbolivar.servicename.exception.ExternalTimeoutException;
import com.raulbolivar.servicename.ports.out.GenericApiResilienceExecutorGateway;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.concurrent.TimeoutException;
import reactor.core.publisher.Mono;

public final class GenericApiResilienceExecutor implements GenericApiResilienceExecutorGateway {

    private final CircuitBreakerRegistry circuitBreakers;
    private final RetryRegistry          retries;
    private final TimeLimiterRegistry    timeLimiters;

    public GenericApiResilienceExecutor(CircuitBreakerRegistry circuitBreakers,
                                        RetryRegistry retries,
                                        TimeLimiterRegistry timeLimiters) {
        this.circuitBreakers = circuitBreakers;
        this.retries = retries;
        this.timeLimiters = timeLimiters;
    }

    @Override
    public <T> Mono<T> execute(String operation, Mono<T> invocation) {
        var timeout = timeLimiters.timeLimiter(operation);
        var retry = retries.retry(operation);
        var circuitBreaker = circuitBreakers.circuitBreaker(operation);

        return invocation
                .transformDeferred(TimeLimiterOperator.of(timeout))
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(TimeoutException.class,
                        error -> new ExternalTimeoutException(operation, error))
                .onErrorMap(CallNotPermittedException.class,
                        error -> new ExternalCircuitOpenException(operation, error));
    }

    public static boolean retryable(Throwable error) {
        return !(error instanceof ExternalClientException)
                && !(error instanceof ExternalResponseMappingException)
                && !(error instanceof ExternalCircuitOpenException);
    }
}
