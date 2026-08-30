package com.raulbolivar.servicename.model;

public record DatabaseSecret(
        String username,
        String password,
        String host,
        String port,
        String database
) {
}
