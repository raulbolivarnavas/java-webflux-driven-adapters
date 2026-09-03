package com.raulbolivar.driven.parameterstore.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AwsParameterStoreProperties.class)
public class AwsParameterStoreConfiguration {

    @Bean
    public SsmAsyncClient ssmAsyncClient(
            AwsParameterStoreProperties properties) {

        var httpClient = NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(3))
                .connectionAcquisitionTimeout(Duration.ofSeconds(3))
                .readTimeout(properties.apiCallTimeout())
                .writeTimeout(properties.apiCallTimeout())
                .build();

        var overrideConfiguration =
                ClientOverrideConfiguration.builder()
                        .apiCallTimeout(
                                properties.apiCallTimeout()
                        )
                        .apiCallAttemptTimeout(
                                properties.apiCallAttemptTimeout()
                        )
                        .build();

        var builder = SsmAsyncClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(
                        DefaultCredentialsProvider.create()
                )
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfiguration);

        if (properties.endpointOverride() != null) {
            builder.endpointOverride(
                    properties.endpointOverride()
            );
        }

        return builder.build();
    }
}