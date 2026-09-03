package com.raulbolivar.driven.parameterstore.actuator;

import com.raulbolivar.driven.parameterstore.adapter.AwsParameterStoreAdapter;
import com.raulbolivar.driven.parameterstore.model.ParameterCacheInfo;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Endpoint(id = "parameterStoreCache")
public class ParameterStoreCacheEndpoint {

    private final AwsParameterStoreAdapter adapter;

    public ParameterStoreCacheEndpoint(
            AwsParameterStoreAdapter adapter) {

        this.adapter = adapter;
    }

    @ReadOperation
    public ParameterCacheInfo cacheInfo() {
        return adapter.cacheInfo();
    }

    @WriteOperation
    public Mono<CacheClearResponse> clearCache() {
        int removedEntries =
                adapter.cacheInfo().size();

        return adapter.clearCache()
                .thenReturn(
                        new CacheClearResponse(
                                true,
                                removedEntries
                        )
                );
    }

    public record CacheClearResponse(
            boolean cleared,
            int removedEntries
    ) {
    }
}