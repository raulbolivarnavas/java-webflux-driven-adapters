package com.raulbolivar.servicename.ports.out;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface SecretsManagerGateway {

    Mono<String> getSecret(String secretId);

    Mono<String> getSecret(String secretId, String versionStage);

    Mono<Map<String, String>> getSecretAsMap(String secretId);

    <T> Mono<T> getSecret(String secretId, Class<T> responseType);

    Mono<Void> invalidate(String secretId);

    void clearCache();
}