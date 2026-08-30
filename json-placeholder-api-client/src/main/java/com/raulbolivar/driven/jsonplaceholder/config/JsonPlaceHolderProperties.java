package com.raulbolivar.driven.jsonplaceholder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jsonplaceholder")
public record JsonPlaceHolderProperties() {
}
