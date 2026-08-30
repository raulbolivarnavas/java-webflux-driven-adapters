package com.raulbolivar.driven.secretsmanager.actuator;

import com.raulbolivar.driven.secretsmanager.adapter.AwsSecretsManagerAdapter;
import com.raulbolivar.servicename.model.SecretsCacheInfo;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "secretsCache")
public class SecretsCacheEndpoint {

    private final AwsSecretsManagerAdapter adapter;

    public SecretsCacheEndpoint(
            AwsSecretsManagerAdapter adapter) {
        this.adapter = adapter;
    }

    @ReadOperation
    public SecretsCacheInfo cacheInfo() {
        return adapter.cacheInfo();
    }

    @WriteOperation
    public CacheClearResponse clearCache() {
        int removedEntries = adapter.cacheInfo().size();

        adapter.clearCache();

        return new CacheClearResponse(
                true,
                removedEntries
        );
    }

    public record CacheClearResponse(
            boolean cleared,
            int removedEntries
    ) {
    }
}