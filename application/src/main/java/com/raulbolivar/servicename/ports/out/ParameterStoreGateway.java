package com.raulbolivar.servicename.ports.out;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface ParameterStoreGateway {

    Mono<String> getParameter(String parameterName);

    Mono<String> getParameter(
            String parameterName,
            boolean withDecryption
    );

    Mono<String> getParameterVersion(
            String parameterName,
            long version,
            boolean withDecryption
    );

    Mono<Map<String, String>> getParameterAsMap(
            String parameterName
    );

    <T> Mono<T> getParameter(
            String parameterName,
            Class<T> responseType
    );

    Mono<Map<String, String>> getParametersByPath(
            String path,
            boolean recursive,
            boolean withDecryption
    );

    Mono<Void> invalidate(String parameterName);

    Mono<Void> invalidateByPath(String path);

    Mono<Void> clearCache();
}
