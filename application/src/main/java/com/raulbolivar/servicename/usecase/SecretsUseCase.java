package com.raulbolivar.servicename.usecase;

import com.raulbolivar.servicename.model.AwsConnectionValue;
import com.raulbolivar.servicename.model.SecretValue;
import com.raulbolivar.servicename.ports.in.ISecretsUseCase;
import com.raulbolivar.servicename.ports.out.SecretsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecretsUseCase implements ISecretsUseCase {

    private final SecretsGateway secrets;

    @Override
    public Mono<SecretValue> getSecretValue(AwsConnectionValue connectionValue, String secretId) {
        return secrets.getSecretValue(connectionValue, secretId);
    }
}
