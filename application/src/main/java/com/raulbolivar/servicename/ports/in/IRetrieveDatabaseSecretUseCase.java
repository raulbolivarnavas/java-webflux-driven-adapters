package com.raulbolivar.servicename.ports.in;

import com.raulbolivar.servicename.model.DatabaseSecret;
import reactor.core.publisher.Mono;

public interface IRetrieveDatabaseSecretUseCase {
    Mono<DatabaseSecret> execute();
}
