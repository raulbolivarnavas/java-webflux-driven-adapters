package com.raulbolivar.driven.parameterstore.model;

import java.time.Duration;
import java.util.List;

public record ParameterCacheInfo(
        boolean enabled,
        int size,
        int loadsInProgress,
        int maximumSize,
        Duration ttl,
        List<Entry> entries
) {

    public record Entry(
            String parameterName,
            String version,
            boolean withDecryption,
            long ageSeconds,
            long remainingSeconds,
            boolean expired
    ) {

        public static Entry from(
                String cacheKey,
                CachedParameter cached,
                Duration ttl) {

            String[] parts = cacheKey.split("::");

            String parameterName = parts[0];

            String version = parts.length > 1
                    ? parts[1]
                    : "latest";

            boolean withDecryption =
                    parts.length > 2
                            && parts[2].endsWith("true");

            long ageNanos = cached.ageNanos();

            return new Entry(
                    parameterName,
                    version,
                    withDecryption,
                    Duration.ofNanos(ageNanos).toSeconds(),
                    Duration.ofNanos(
                            cached.remainingNanos(ttl)
                    ).toSeconds(),
                    cached.isExpired(ttl)
            );
        }
    }
}
