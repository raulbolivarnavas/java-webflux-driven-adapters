package com.raulbolivar.servicename.usecase;

import com.raulbolivar.servicename.model.DatabaseSecret;
import com.raulbolivar.servicename.ports.in.IRetrieveDatabaseSecretUseCase;
import com.raulbolivar.servicename.ports.out.SecretsManagerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RetrieveDatabaseSecretUseCase implements IRetrieveDatabaseSecretUseCase {

    private final SecretsManagerGateway secretsManagerGateway;

    @Override
    public Mono<DatabaseSecret> execute() {
        return secretsManagerGateway.getSecret(
                "database-credentials-base",
                DatabaseSecret.class
        );
    }
}
