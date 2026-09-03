package com.raulbolivar.servicename.ports.in;

import reactor.core.publisher.Mono;

public interface IParameterStoreUseCase {

    Mono<String> getParameters(String parameterName);
}
