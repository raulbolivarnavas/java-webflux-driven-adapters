package com.raulbolivar.servicename.driver.api.handler;

import com.raulbolivar.servicename.driver.api.dto.StoredProcedureRequestDto;
import com.raulbolivar.servicename.driver.api.mapper.SpExecutorMapper;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import com.raulbolivar.servicename.ports.in.IStoredProcedureExecutorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SpExecutorHandler {

    private final IStoredProcedureExecutorUseCase storedProcedureExecutor;
    private final SpExecutorMapper mapper;

    public Mono<ServerResponse> execute(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(StoredProcedureRequestDto.class)
                .map(request -> new StoredProcedureCommand(request.parameters()))
                .flatMap(storedProcedureExecutor::execute)
                .flatMap(result -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(result));
    }
}
