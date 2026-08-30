package com.raulbolivar.servicename.ports.out;

import com.raulbolivar.servicename.model.ProcedureResponse;
import com.raulbolivar.servicename.model.ProcedureResultDefinition;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import reactor.core.publisher.Mono;

public interface StoredProcedureExecutorGateway {

    Mono<ProcedureResponse> execute(
            StoredProcedureCommand command,
            String spContent,
            ProcedureResultDefinition definition
    );
}
