package com.raulbolivar.driven.secretsmanager.config;

//import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AwsSecretsManagerProperties.class)
public class AwsSecretsManagerConfiguration {

    @Bean
    public SecretsManagerAsyncClient secretsManagerAsyncClient(
            AwsSecretsManagerProperties properties) {

        var httpClient = NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(3))
                .readTimeout(properties.apiCallTimeout())
                .writeTimeout(properties.apiCallTimeout())
                .connectionAcquisitionTimeout(Duration.ofSeconds(3))
                .putChannelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS,3_000)
                .build();

        var overrideConfiguration = ClientOverrideConfiguration.builder()
                .apiCallTimeout(properties.apiCallTimeout())
                .apiCallAttemptTimeout(properties.apiCallAttemptTimeout())
                .build();

        var builder = SecretsManagerAsyncClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfiguration);

        if (properties.endpointOverride() != null) {
            builder.endpointOverride(properties.endpointOverride());
        }

        return builder.build();
    }
}