package com.raulbolivar.servicename.ports.in;

import com.raulbolivar.servicename.model.ProcedureResponse;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import com.raulbolivar.servicename.model.StoredProcedureResult;
import reactor.core.publisher.Mono;

public interface IStoredProcedureExecutorUseCase {

    Mono<ProcedureResponse> execute(StoredProcedureCommand command);
}
