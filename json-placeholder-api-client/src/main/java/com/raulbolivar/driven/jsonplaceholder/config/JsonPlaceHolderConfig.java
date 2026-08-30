package com.raulbolivar.driven.jsonplaceholder.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JsonPlaceHolderProperties.class)
public class JsonPlaceHolderConfig {
}
