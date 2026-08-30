package com.raulbolivar.servicename.ports.in;

import com.raulbolivar.servicename.model.AwsConnectionValue;
import com.raulbolivar.servicename.model.SecretValue;
import reactor.core.publisher.Mono;

public interface ISecretsUseCase {
    Mono<SecretValue> getSecretValue(AwsConnectionValue connectionValue, String secretId);
}
