package com.raulbolivar.lib.client.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GenericApiClientProperties.class)
public class GenericApiClientConfiguration {

    @Bean
    WebClient genericWebClient(GenericApiClientProperties properties) {
        var transport = properties.transport();
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(transport.connectTimeout().toMillis()))
                .responseTimeout(transport.readTimeout())
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(
                                transport.readTimeout().toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(
                                transport.writeTimeout().toMillis(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(transport.maxInMemorySize()))
                .build();
    }
}