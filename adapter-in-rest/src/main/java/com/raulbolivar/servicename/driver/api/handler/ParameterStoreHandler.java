package com.raulbolivar.servicename.driver.api.handler;

import com.raulbolivar.servicename.ports.in.IParameterStoreUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParameterStoreHandler {

    private final IParameterStoreUseCase parameterStore;

    public Mono<ServerResponse> getParameters(ServerRequest serverRequest) {
        String parameterName = serverRequest.pathVariable("parameterName");
        return parameterStore.getParameters(parameterName)
                .flatMap(parameters -> ServerResponse.ok()
                        .bodyValue(parameters));
    }
}
