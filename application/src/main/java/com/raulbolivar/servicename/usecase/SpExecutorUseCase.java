package com.raulbolivar.servicename.usecase;

import com.raulbolivar.servicename.model.ProcedureResponse;
import com.raulbolivar.servicename.model.ProcedureResultDefinition;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import com.raulbolivar.servicename.ports.in.IStoredProcedureExecutorUseCase;
import com.raulbolivar.servicename.ports.out.StoredProcedureDecoder;
import com.raulbolivar.servicename.ports.out.StoredProcedureExecutorGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpExecutorUseCase implements IStoredProcedureExecutorUseCase {

    @Value("${stored-procedure.sp-content}")
    private String spContent;

    private final StoredProcedureExecutorGateway executorGateway;
    private final StoredProcedureDecoder decoder;

    @Override
    public Mono<ProcedureResponse> execute(StoredProcedureCommand command) {
        log.info("Executing stored procedure with command: {}", command);
        String decodedSpContent = decoder.decode(spContent);

        ProcedureResultDefinition definition = new ProcedureResultDefinition(
                "CodigoError",
                "MensajeError",
                "ReturnValue",
                "-1"
        );

        return executorGateway.execute(command, decodedSpContent, definition)
                .doOnSuccess(response -> log.info("Stored procedure executed successfully with response: {}", response))
                .doOnError(error -> log.error("Error executing stored procedure", error));
    }
}
