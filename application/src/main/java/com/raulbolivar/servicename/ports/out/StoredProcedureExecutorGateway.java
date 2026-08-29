package com.raulbolivar.servicename.ports.out;

import com.raulbolivar.servicename.model.StoredProcedureCommand;
import com.raulbolivar.servicename.model.StoredProcedureResult;
import reactor.core.publisher.Mono;

public interface StoredProcedureExecutorGateway {
    Mono<StoredProcedureResult> execute(StoredProcedureCommand command, String spContent);
}
