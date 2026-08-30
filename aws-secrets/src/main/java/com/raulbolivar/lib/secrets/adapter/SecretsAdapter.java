package com.raulbolivar.lib.secrets.adapter;

import com.raulbolivar.servicename.exception.*;
import com.raulbolivar.servicename.model.AwsConnectionValue;
import com.raulbolivar.servicename.model.Secret;
import com.raulbolivar.servicename.model.SecretValue;
import com.raulbolivar.servicename.ports.out.SecretsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.*;
import software.amazon.awssdk.services.ssooidc.model.AccessDeniedException;

import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class SecretsAdapter implements SecretsGateway {

    @Override
    public Mono<SecretValue> getSecretValue(AwsConnectionValue connectionValue, String secretArnOrName) {
        log.info("Getting secret value for secretId: {}", secretArnOrName);
        return validate(connectionValue.accessKeyId(), connectionValue.secretAccessKey(), connectionValue.region())
                .then(validateSecretId(secretArnOrName))
                .then(Mono.defer(() -> {
                    SecretsManagerAsyncClient client = buildClient(
                            connectionValue.region(),
                            connectionValue.accessKeyId(),
                            connectionValue.secretAccessKey(),
                            connectionValue.sessionToken()
                    );

                    GetSecretValueRequest request = GetSecretValueRequest.builder()
                            .secretId(secretArnOrName)
                            .build();

                    return Mono.fromFuture(client.getSecretValue(request))
                            .map(response -> new SecretValue(
                                    response.name(),
                                    response.arn(),
                                    response.secretString(),
                                    response.versionId(),
                                    response.versionStages(),
                                    response.createdDate() != null
                                            ? response.createdDate().toString()
                                            : null
                            ))
                            .doFinally(signal -> client.close());
                }))
                .onErrorMap(this::mapException);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Mono<Void> validateSecretId(String secretId) {
        if (secretId == null || secretId.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta secretId"));
        }

        return Mono.empty();
    }

    private Secret toSecret(SecretListEntry entry) {
        return new Secret(
                entry.name(),
                entry.arn(),
                entry.description(),
                entry.lastChangedDate() != null ? entry.lastChangedDate().toString() : null,
                entry.lastAccessedDate() != null ? entry.lastAccessedDate().toString() : null,
                entry.createdDate() != null ? entry.createdDate().toString() : null,
                entry.deletedDate() != null ? entry.deletedDate().toString() : null
        );
    }

    private SecretsManagerAsyncClient buildClient(String region,
                                                  String accessKeyId,
                                                  String secretAccessKey,
                                                  String sessionToken) {

        var credentials = sessionToken != null && !sessionToken.isBlank()
                ? AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                : AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return SecretsManagerAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private Mono<Void> validate(String accessKeyId,
                                String secretAccessKey,
                                String region) {

        if (accessKeyId == null || accessKeyId.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta accessKeyId"));
        }

        if (secretAccessKey == null || secretAccessKey.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta secretAccessKey"));
        }

        if (region == null || region.isBlank()) {
            return Mono.error(new SecretsBadRequestException("Falta region AWS"));
        }

        return Mono.empty();
    }

    private Throwable mapException(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof SecretsBadRequestException
                || cause instanceof SecretsAccessDeniedException
                || cause instanceof SecretsClientException
                || cause instanceof SecretsTimeoutException) {
            return cause;
        }

        if (cause instanceof AccessDeniedException
                || cause instanceof UnrecognizedClientException
                || cause instanceof InvalidRequestException
                || cause instanceof InvalidParameterException) {
            return new SecretsAccessDeniedException("No tiene permisos o las credenciales AWS no son válidas", cause);
        }

        if (cause instanceof ResourceNotFoundException) {
            return new SecretsClientException("No se encontró el recurso solicitado en Secrets Manager", cause);
        }

        if (cause instanceof SdkClientException) {
            return new SecretsClientException("Error de comunicación con AWS Secrets Manager", cause);
        }

        return new SecretsClientException("Error inesperado consultando AWS Secrets Manager", cause);
    }

    private Throwable unwrap(Throwable error) {
        if (error instanceof CompletionException && error.getCause() != null) {
            return error.getCause();
        }

        if (error.getCause() instanceof CompletionException completionException
                && completionException.getCause() != null) {
            return completionException.getCause();
        }

        return error;
    }
}
