package com.raulbolivar.driven.parameterstore.adapter;

import com.raulbolivar.driven.parameterstore.config.AwsParameterStoreProperties;
import com.raulbolivar.driven.parameterstore.model.CachedParameter;
import com.raulbolivar.driven.parameterstore.model.ParameterCacheInfo;
import com.raulbolivar.servicename.exception.ParameterMappingException;
import com.raulbolivar.servicename.exception.ParameterNotFoundException;
import com.raulbolivar.servicename.exception.ParameterStoreAccessException;
import com.raulbolivar.servicename.ports.out.ParameterStoreGateway;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.SsmException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AwsParameterStoreAdapter implements ParameterStoreGateway {

    private final SsmAsyncClient client;
    private final AwsParameterStoreProperties properties;
    private final JsonMapper jsonMapper;

    private final Map<String, CachedParameter> cache =
            new ConcurrentHashMap<>();

    /*
     * Evita que varias solicitudes concurrentes para la misma
     * clave produzcan múltiples llamadas a AWS.
     */
    private final Map<String, Mono<String>> inFlight =
            new ConcurrentHashMap<>();

    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Counter awsRequests;
    private final Counter loadErrors;

    public AwsParameterStoreAdapter(
            SsmAsyncClient client,
            AwsParameterStoreProperties properties,
            JsonMapper jsonMapper,
            MeterRegistry meterRegistry) {

        this.client = client;
        this.properties = properties;
        this.jsonMapper = jsonMapper;

        Tags tags = Tags.of(
                "provider", "aws",
                "service", "ssm-parameter-store"
        );

        this.cacheHits = meterRegistry.counter(
                "aws.parameter.store.cache.hits",
                tags
        );

        this.cacheMisses = meterRegistry.counter(
                "aws.parameter.store.cache.misses",
                tags
        );

        this.awsRequests = meterRegistry.counter(
                "aws.parameter.store.requests",
                tags
        );

        this.loadErrors = meterRegistry.counter(
                "aws.parameter.store.load.errors",
                tags
        );

        meterRegistry.gauge(
                "aws.parameter.store.cache.size",
                tags,
                cache,
                Map::size
        );
    }

    @Override
    public Mono<String> getParameter(String parameterName) {
        return getParameter(parameterName, true);
    }

    @Override
    public Mono<String> getParameter(
            String parameterName,
            boolean withDecryption) {

        return getCachedOrLoad(
                cacheKey(
                        parameterName,
                        null,
                        withDecryption
                ),
                parameterName,
                () -> retrieveParameter(
                        parameterName,
                        withDecryption
                )
        );
    }

    @Override
    public Mono<String> getParameterVersion(
            String parameterName,
            long version,
            boolean withDecryption) {

        if (version <= 0) {
            return Mono.error(
                    new IllegalArgumentException(
                            "version debe ser mayor que cero"
                    )
            );
        }

        String versionedName =
                parameterName + ":" + version;

        return getCachedOrLoad(
                cacheKey(
                        parameterName,
                        version,
                        withDecryption
                ),
                parameterName,
                () -> retrieveParameter(
                        versionedName,
                        withDecryption
                )
        );
    }

    @Override
    public Mono<Map<String, String>> getParameterAsMap(
            String parameterName) {

        return getParameter(parameterName)
                .map(value ->
                        deserializeMap(
                                parameterName,
                                value
                        )
                );
    }

    @Override
    public <T> Mono<T> getParameter(
            String parameterName,
            Class<T> responseType) {

        if (responseType == null) {
            return Mono.error(
                    new IllegalArgumentException(
                            "responseType es obligatorio"
                    )
            );
        }

        return getParameter(parameterName)
                .map(value ->
                        deserialize(
                                parameterName,
                                value,
                                responseType
                        )
                );
    }

    @Override
    public Mono<Map<String, String>> getParametersByPath(
            String path,
            boolean recursive,
            boolean withDecryption) {

        return Mono.defer(() -> {
            validateParameterName(path);

            Map<String, String> result =
                    new LinkedHashMap<>();

            return retrievePage(
                    path,
                    recursive,
                    withDecryption,
                    null,
                    result
            ).thenReturn(Map.copyOf(result));
        });
    }

    @Override
    public Mono<Void> invalidate(String parameterName) {
        return Mono.fromRunnable(() -> {
            validateParameterName(parameterName);

            cache.keySet().removeIf(
                    key -> key.startsWith(
                            parameterName + "::"
                    )
            );

            inFlight.keySet().removeIf(
                    key -> key.startsWith(
                            parameterName + "::"
                    )
            );

            log.info(
                    "[PARAMETER-STORE] Cache invalidated. parameterName={}",
                    parameterName
            );
        });
    }

    @Override
    public Mono<Void> invalidateByPath(String path) {
        return Mono.fromRunnable(() -> {
            validateParameterName(path);

            cache.keySet().removeIf(
                    key -> key.startsWith(path)
            );

            inFlight.keySet().removeIf(
                    key -> key.startsWith(path)
            );

            log.info(
                    "[PARAMETER-STORE] Cache invalidated by path. path={}",
                    path
            );
        });
    }

    @Override
    public Mono<Void> clearCache() {
        return Mono.fromRunnable(() -> {
            cache.clear();
            inFlight.clear();

            log.info(
                    "[PARAMETER-STORE] Cache cleared"
            );
        });
    }

    public ParameterCacheInfo cacheInfo() {
        Duration ttl = properties.cache().ttl();

        var entries = cache.entrySet()
                .stream()
                .map(entry ->
                        ParameterCacheInfo.Entry.from(
                                entry.getKey(),
                                entry.getValue(),
                                ttl
                        )
                )
                .toList();

        return new ParameterCacheInfo(
                properties.cache().enabled(),
                cache.size(),
                inFlight.size(),
                properties.cache().maximumSize(),
                ttl,
                entries
        );
    }

    private Mono<String> getCachedOrLoad(
            String cacheKey,
            String parameterName,
            ParameterLoader loader) {

        return Mono.defer(() -> {
            validateParameterName(parameterName);

            String cachedValue =
                    findCachedValue(cacheKey);

            if (cachedValue != null) {
                cacheHits.increment();

                log.debug(
                        "[PARAMETER-STORE] Cache hit. parameterName={}",
                        parameterName
                );

                return Mono.just(cachedValue);
            }

            cacheMisses.increment();

            return inFlight.computeIfAbsent(
                    cacheKey,
                    ignored -> createSharedLoad(
                            cacheKey,
                            parameterName,
                            loader
                    )
            );
        });
    }

    private Mono<String> createSharedLoad(
            String cacheKey,
            String parameterName,
            ParameterLoader loader) {

        return loader.load()
                .doOnNext(value ->
                        cacheValue(cacheKey, value)
                )
                .doOnError(error -> {
                    loadErrors.increment();

                    log.error(
                            "[PARAMETER-STORE] Error loading parameter. parameterName={}",
                            parameterName,
                            error
                    );
                })
                .doFinally(signalType ->
                        inFlight.remove(cacheKey)
                )
                .cache();
    }

    private String findCachedValue(String cacheKey) {
        if (!properties.cache().enabled()) {
            return null;
        }

        CachedParameter cached = cache.get(cacheKey);

        if (cached == null) {
            return null;
        }

        if (cached.isExpired(properties.cache().ttl())) {
            cache.remove(cacheKey, cached);
            return null;
        }

        return cached.value();
    }

    private void cacheValue(
            String cacheKey,
            String value) {

        if (!properties.cache().enabled()) {
            return;
        }

        evictIfNecessary();

        cache.put(
                cacheKey,
                CachedParameter.of(value)
        );
    }

    private void evictIfNecessary() {
        int maximumSize =
                properties.cache().maximumSize();

        if (cache.size() < maximumSize) {
            return;
        }

        cache.entrySet()
                .stream()
                .min((left, right) ->
                        Long.compare(
                                left.getValue().createdAtNanos(),
                                right.getValue().createdAtNanos()
                        )
                )
                .ifPresent(entry ->
                        cache.remove(
                                entry.getKey(),
                                entry.getValue()
                        )
                );
    }

    private Mono<String> retrieveParameter(
            String parameterName,
            boolean withDecryption) {

        GetParameterRequest request =
                GetParameterRequest.builder()
                        .name(parameterName)
                        .withDecryption(withDecryption)
                        .build();

        awsRequests.increment();

        log.debug(
                "[PARAMETER-STORE] Retrieving parameter. parameterName={}, withDecryption={}",
                parameterName,
                withDecryption
        );

        return Mono.fromFuture(() ->
                        client.getParameter(request)
                )
                .map(response -> {
                    if (response.parameter() == null
                            || response.parameter().value() == null) {

                        throw new ParameterStoreAccessException(
                                parameterName,
                                "AWS Parameter Store devolvió un parámetro sin valor",
                                null
                        );
                    }

                    return response.parameter().value();
                })
                .onErrorMap(error ->
                        mapException(
                                parameterName,
                                error
                        )
                );
    }

    private Mono<Void> retrievePage(
            String path,
            boolean recursive,
            boolean withDecryption,
            String nextToken,
            Map<String, String> accumulator) {

        GetParametersByPathRequest request =
                GetParametersByPathRequest.builder()
                        .path(path)
                        .recursive(recursive)
                        .withDecryption(withDecryption)
                        .nextToken(nextToken)
                        .build();

        awsRequests.increment();

        return Mono.fromFuture(() ->
                        client.getParametersByPath(request)
                )
                .onErrorMap(error ->
                        mapException(path, error)
                )
                .flatMap(response -> {
                    response.parameters().forEach(parameter -> {
                        accumulator.put(
                                parameter.name(),
                                parameter.value()
                        );

                        cacheValue(
                                cacheKey(
                                        parameter.name(),
                                        parameter.version(),
                                        withDecryption
                                ),
                                parameter.value()
                        );
                    });

                    if (response.nextToken() == null
                            || response.nextToken().isBlank()) {
                        return Mono.empty();
                    }

                    return retrievePage(
                            path,
                            recursive,
                            withDecryption,
                            response.nextToken(),
                            accumulator
                    );
                });
    }

    private Map<String, String> deserializeMap(
            String parameterName,
            String value) {

        try {
            return jsonMapper.readValue(
                    value,
                    new TypeReference<Map<String, String>>() {
                    }
            );
        } catch (Exception exception) {
            throw new ParameterMappingException(
                    parameterName,
                    Map.class,
                    exception
            );
        }
    }

    private <T> T deserialize(
            String parameterName,
            String value,
            Class<T> responseType) {

        try {
            return jsonMapper.readValue(
                    value,
                    responseType
            );
        } catch (Exception exception) {
            throw new ParameterMappingException(
                    parameterName,
                    responseType,
                    exception
            );
        }
    }

    private Throwable mapException(
            String parameterName,
            Throwable throwable) {

        Throwable cause = unwrap(throwable);

        if (cause instanceof ParameterNotFoundException) {
            return new com.raulbolivar.servicename.exception
                    .ParameterNotFoundException(
                    parameterName,
                    cause
            );
        }

        if (cause instanceof SdkClientException) {
            return new ParameterStoreAccessException(
                    parameterName,
                    "No fue posible establecer comunicación con AWS Parameter Store",
                    cause
            );
        }

        if (cause instanceof SsmException exception) {
            return new ParameterStoreAccessException(
                    parameterName,
                    "AWS Parameter Store rechazó la solicitud. statusCode=%s"
                            .formatted(
                                    exception.statusCode()
                            ),
                    exception
            );
        }

        if (cause instanceof ParameterStoreAccessException) {
            return cause;
        }

        return new ParameterStoreAccessException(
                parameterName,
                "Error inesperado consultando AWS Parameter Store",
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

    private void validateParameterName(String parameterName) {
        if (parameterName == null
                || parameterName.isBlank()) {

            throw new IllegalArgumentException(
                    "parameterName es obligatorio"
            );
        }
    }

    private String cacheKey(
            String parameterName,
            Long version,
            boolean withDecryption) {

        String versionValue = version == null
                ? "latest"
                : version.toString();

        return "%s::%s::decrypt=%s".formatted(
                parameterName,
                versionValue,
                withDecryption
        );
    }

    @FunctionalInterface
    private interface ParameterLoader {

        Mono<String> load();
    }
}