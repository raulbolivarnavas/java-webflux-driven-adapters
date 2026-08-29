package com.raulbolivar.servicename.driven.spexecutor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "database")
public record DatabaseProperties(
        String host,
        int port,
        String name,
        String username,
        String password,
        Security security
) {
    public record Security(
            boolean sslEnabled,
            boolean trustServerCertificate,
            String trustStore,
            String trustStorePassword,
            String trustStoreType,
            String hostNameInCertificate
    ) {
    }
}
