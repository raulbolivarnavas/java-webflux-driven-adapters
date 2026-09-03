package com.raulbolivar.driven.parameterstore.model;

import java.time.Duration;

public record CachedParameter(
        String value,
        long createdAtNanos
) {

    public static CachedParameter of(String value) {
        return new CachedParameter(
                value,
                System.nanoTime()
        );
    }

    public boolean isExpired(Duration ttl) {
        return ageNanos() >= ttl.toNanos();
    }

    public long ageNanos() {
        return System.nanoTime() - createdAtNanos;
    }

    public long remainingNanos(Duration ttl) {
        return Math.max(
                0,
                ttl.toNanos() - ageNanos()
        );
    }
}