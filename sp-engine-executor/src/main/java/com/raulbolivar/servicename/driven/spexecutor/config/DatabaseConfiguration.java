package com.raulbolivar.servicename.driven.spexecutor.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseConfiguration {

    private static final Option<Boolean> TRUST_SERVER_CERTIFICATE =
            Option.valueOf("trustServerCertificate");

    private static final Option<String> TRUST_STORE =
            Option.valueOf("trustStore");

    private static final Option<String> TRUST_STORE_PASSWORD =
            Option.sensitiveValueOf("trustStorePassword");

    private static final Option<String> TRUST_STORE_TYPE =
            Option.valueOf("trustStoreType");

    private static final Option<String> HOST_NAME_IN_CERTIFICATE =
            Option.valueOf("hostNameInCertificate");

    @Bean
    public ConnectionFactory connectionFactory(DatabaseProperties properties) {
        Builder builder = ConnectionFactoryOptions.builder()
                        .option(DRIVER,   "sqlserver")
                        .option(HOST,     properties.host())
                        .option(PORT,     properties.port())
                        .option(DATABASE, properties.name())
                        .option(USER,     properties.username())
                        .option(PASSWORD, properties.password())
                        .option(SSL,      properties.security().sslEnabled());

        configureSecurity(builder, properties.security());

        return ConnectionFactories.get(builder.build());
    }

    private void configureSecurity(Builder builder,
                                   DatabaseProperties.Security security) {
        if (!security.sslEnabled()) {
            return;
        }

        builder.option(TRUST_SERVER_CERTIFICATE, security.trustServerCertificate());

        if (hasText(security.trustStore())) {
            builder.option(TRUST_STORE, security.trustStore());
        }

        if (hasText(security.trustStorePassword())) {
            builder.option(TRUST_STORE_PASSWORD, security.trustStorePassword());
        }

        if (hasText(security.trustStoreType())) {
            builder.option(TRUST_STORE_TYPE, security.trustStoreType());
        }

        if (hasText(security.hostNameInCertificate())) {
            builder.option(HOST_NAME_IN_CERTIFICATE, security.hostNameInCertificate());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}