package com.raulbolivar.servicename.usecase;

import com.raulbolivar.servicename.ports.in.IParameterStoreUseCase;
import com.raulbolivar.servicename.ports.out.ParameterStoreGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParameterStoreUseCase implements IParameterStoreUseCase {

    private final ParameterStoreGateway parameterStore;

    @Override
    public Mono<String> getParameters(String parameterName) {
        return parameterStore.getParameter(parameterName);
    }
}
