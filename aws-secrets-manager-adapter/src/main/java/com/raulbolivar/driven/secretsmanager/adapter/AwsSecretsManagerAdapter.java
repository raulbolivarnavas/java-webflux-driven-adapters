package com.raulbolivar.driven.secretsmanager.adapter;

import com.raulbolivar.driven.secretsmanager.config.AwsSecretsManagerProperties;
import com.raulbolivar.driven.secretsmanager.mapper.SecretValueMapper;
import com.raulbolivar.servicename.exception.SecretAccessException;
import com.raulbolivar.servicename.exception.SecretNotFoundException;
import com.raulbolivar.servicename.ports.out.SecretsManagerGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.DecryptionFailureException;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import com.raulbolivar.servicename.model.SecretsCacheInfo;

import java.util.List;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class AwsSecretsManagerAdapter implements SecretsManagerGateway {

    private static final String DEFAULT_VERSION_STAGE = "AWSCURRENT";

    private final SecretsManagerAsyncClient   client;
    private final AwsSecretsManagerProperties properties;
    private final SecretValueMapper           mapper;

    private final Map<String, CachedSecret> cache =
            new ConcurrentHashMap<>();

    public AwsSecretsManagerAdapter(SecretsManagerAsyncClient client,
                                    AwsSecretsManagerProperties properties,
                                    SecretValueMapper mapper) {
        this.client     = client;
        this.properties = properties;
        this.mapper     = mapper;
    }

    @Override
    public Mono<String> getSecret(String secretId) {
        return getSecret(secretId, DEFAULT_VERSION_STAGE);
    }

    @Override
    public Mono<String> getSecret(String secretId, String versionStage) {
        return Mono.defer(() -> {
            validateSecretId(secretId);

            String normalizedStage = normalizeVersionStage(versionStage);
            String cacheKey = cacheKey(secretId, normalizedStage);

            String cachedValue = findCachedValue(cacheKey);

            if (cachedValue != null) {
                log.debug(
                        "[SECRETS-MANAGER] Secret obtained from cache. secretId={}, versionStage={}",
                        secretId,
                        normalizedStage
                );

                return Mono.just(cachedValue);
            }

            return retrieveFromAws(secretId, normalizedStage)
                    .doOnNext(value -> cacheSecret(cacheKey, value));
        });
    }

    @Override
    public Mono<Map<String, String>> getSecretAsMap(String secretId) {
        return getSecret(secretId)
                .map(value -> mapper.toMap(secretId, value));
    }

    @Override
    public <T> Mono<T> getSecret(String secretId, Class<T> responseType) {
        if (responseType == null) {
            return Mono.error(
                    new IllegalArgumentException(
                            "responseType es obligatorio"
                    )
            );
        }

        return getSecret(secretId)
                .map(value -> mapper.toObject(
                        secretId,
                        value,
                        responseType
                ));
    }

    @Override
    public Mono<Void> invalidate(String secretId) {
        return Mono.fromRunnable(() ->
                cache.keySet().removeIf(
                        key -> key.startsWith(secretId + "::")
                )
        );
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    private Mono<String> retrieveFromAws(String secretId, String versionStage) {
        GetSecretValueRequest request =
                GetSecretValueRequest.builder()
                        .secretId(secretId)
                        .versionStage(versionStage)
                        .build();

        log.debug(
                "[SECRETS-MANAGER] Retrieving secret. secretId={}, versionStage={}",
                secretId,
                versionStage
        );

        return Mono.fromFuture(() -> client.getSecretValue(request))
                .map(response ->
                        mapper.extractValue(secretId, response)
                )
                .doOnSuccess(ignored ->
                        log.debug(
                                "[SECRETS-MANAGER] Secret retrieved successfully. secretId={}",
                                secretId
                        )
                )
                .onErrorMap(error ->
                        mapException(secretId, error)
                );
    }

    private Throwable mapException(String secretId, Throwable throwable) {
        Throwable cause = unwrap(throwable);

        if (cause instanceof ResourceNotFoundException) {
            return new SecretNotFoundException(
                    secretId,
                    cause
            );
        }

        if (cause instanceof DecryptionFailureException) {
            return new SecretAccessException(
                    secretId,
                    "AWS KMS no pudo descifrar el secreto",
                    cause
            );
        }

        if (cause instanceof InvalidRequestException) {
            return new SecretAccessException(
                    secretId,
                    "La solicitud del secreto no es válida",
                    cause
            );
        }

        if (cause instanceof SdkClientException) {
            return new SecretAccessException(
                    secretId,
                    "No fue posible establecer comunicación con AWS Secrets Manager",
                    cause
            );
        }

        if (cause instanceof SecretsManagerException exception) {
            return new SecretAccessException(
                    secretId,
                    "AWS Secrets Manager rechazó la solicitud. statusCode=%s"
                            .formatted(exception.statusCode()),
                    exception
            );
        }

        return new SecretAccessException(
                secretId,
                "Error inesperado consultando AWS Secrets Manager",
                cause
        );
    }

    private Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException
                && throwable.getCause() != null) {
            return throwable.getCause();
        }

        return throwable;
    }

    private String findCachedValue(String cacheKey) {
        if (!properties.cache().enabled()) {
            return null;
        }

        CachedSecret cachedSecret = cache.get(cacheKey);

        if (cachedSecret == null) {
            return null;
        }

        if (cachedSecret.isExpired(properties.cache().ttl())) {
            cache.remove(cacheKey);
            return null;
        }

        return cachedSecret.value();
    }

    private void cacheSecret(String cacheKey, String value) {
        if (properties.cache().enabled()) {
            cache.put(
                    cacheKey,
                    new CachedSecret(
                            value,
                            System.nanoTime()
                    )
            );
        }
    }

    private void validateSecretId(String secretId) {
        if (secretId == null || secretId.isBlank()) {
            throw new IllegalArgumentException(
                    "secretId es obligatorio"
            );
        }
    }

    private String normalizeVersionStage(String versionStage) {
        return versionStage == null || versionStage.isBlank()
                ? DEFAULT_VERSION_STAGE
                : versionStage;
    }

    private String cacheKey(String secretId, String versionStage) {
        return secretId + "::" + versionStage;
    }

    private record CachedSecret(String value, long createdAtNanos) {
        private boolean isExpired(Duration ttl) {
            long elapsed = System.nanoTime() - createdAtNanos;
            return elapsed >= ttl.toNanos();
        }
    }

    // ─── Methods for retrieving cache information ────────────────────────────────────────────────

    public SecretsCacheInfo cacheInfo() {
        Duration ttl = properties.cache().ttl();

        List<SecretsCacheInfo.CacheEntryInfo> entries = cache.entrySet()
                .stream()
                .map(entry -> toCacheEntryInfo(
                        entry.getKey(),
                        entry.getValue(),
                        ttl
                ))
                .toList();

        return new SecretsCacheInfo(
                properties.cache().enabled(),
                cache.size(),
                ttl,
                entries
        );
    }

    private SecretsCacheInfo.CacheEntryInfo toCacheEntryInfo(
            String cacheKey,
            CachedSecret cachedSecret,
            Duration ttl) {

        String[] parts = cacheKey.split("::", 2);

        String secretId = parts[0];
        String versionStage = parts.length > 1
                ? parts[1]
                : DEFAULT_VERSION_STAGE;

        long ageNanos =
                System.nanoTime() - cachedSecret.createdAtNanos();

        long remainingNanos =
                Math.max(0, ttl.toNanos() - ageNanos);

        return new SecretsCacheInfo.CacheEntryInfo(
                secretId,
                versionStage,
                Duration.ofNanos(ageNanos).toSeconds(),
                Duration.ofNanos(remainingNanos).toSeconds(),
                ageNanos >= ttl.toNanos()
        );
    }
}