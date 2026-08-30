package com.raulbolivar.servicename.model;

import java.time.Duration;
import java.util.List;

public record SecretsCacheInfo(
        boolean enabled,
        int size,
        Duration ttl,
        List<CacheEntryInfo> entries
) {

    public record CacheEntryInfo(
            String secretId,
            String versionStage,
            long ageSeconds,
            long remainingSeconds,
            boolean expired
    ) {
    }
}
