package com.raulbolivar.servicename.driver.api.handler;

import com.raulbolivar.servicename.ports.in.IRetrievePostsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RetrievePostsHandler {

    private final IRetrievePostsUseCase useCase;

    public Mono<ServerResponse> retrieveAll(ServerRequest request) {
        return ServerResponse.ok()
                .body(useCase.retrieveAll(), Object.class);
    }
}
