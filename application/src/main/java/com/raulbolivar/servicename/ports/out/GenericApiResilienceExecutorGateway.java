package com.raulbolivar.servicename.ports.out;

import reactor.core.publisher.Mono;

public interface GenericApiResilienceExecutorGateway {
    <T> Mono<T> execute(String operation, Mono<T> invocation);
}
