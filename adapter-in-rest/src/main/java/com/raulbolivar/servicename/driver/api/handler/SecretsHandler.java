package com.raulbolivar.servicename.driver.api.handler;

import com.raulbolivar.servicename.driver.api.helper.RequestValidation;
import com.raulbolivar.servicename.driver.api.mapper.SecretsMapper;
import com.raulbolivar.servicename.exception.SecretsBadRequestException;
import com.raulbolivar.servicename.model.AwsConnectionValue;
import com.raulbolivar.servicename.ports.in.IRetrieveDatabaseSecretUseCase;
import com.raulbolivar.servicename.ports.in.ISecretsUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecretsHandler extends RequestValidation {

    private final IRetrieveDatabaseSecretUseCase retrieveDatabaseSecret;
    private final ISecretsUseCase secrets;
    private final SecretsMapper   mapper;

    public Mono<ServerResponse> getSecretValue(ServerRequest request) {
        String accessKeyId     = header(request, "x-aws-access-key-id");
        String secretAccessKey = header(request, "x-aws-secret-access-key");
        String sessionToken    = header(request, "x-aws-session-token");
        String region          = blankFallback(header(request, "x-aws-region"), "us-east-1");
        String secretArnOrName = header(request, "x-secret-id");

        if (accessKeyId == null || secretAccessKey == null) {
            return Mono.error(new SecretsBadRequestException("Faltan credenciales AWS"));
        }

        if (secretArnOrName == null || secretArnOrName.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta header x-secret-id"));
        }

        AwsConnectionValue connectionValue = new AwsConnectionValue(
                accessKeyId,
                secretAccessKey,
                sessionToken,
                region
        );

        return secrets.getSecretValue(connectionValue, secretArnOrName)
                .map(mapper::toSecretValueResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    public Mono<ServerResponse> getDatabaseSecret(ServerRequest request) {
        return retrieveDatabaseSecret.execute()
                .map(mapper::toDatabaseSecretResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
