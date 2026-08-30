package com.raulbolivar.servicename.ports.out;

import com.raulbolivar.servicename.model.AwsConnectionValue;
import com.raulbolivar.servicename.model.SecretValue;
import reactor.core.publisher.Mono;

public interface SecretsGateway {
    Mono<SecretValue> getSecretValue(AwsConnectionValue connectionValue, String secretId);
}
