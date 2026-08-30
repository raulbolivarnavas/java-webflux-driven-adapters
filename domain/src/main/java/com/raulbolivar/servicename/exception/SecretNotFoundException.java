package com.raulbolivar.servicename.exception;

public class SecretNotFoundException extends RuntimeException {

    private final String secretId;

    public SecretNotFoundException(String secretId, Throwable cause) {
        super("No se encontró el secreto solicitado: " + secretId, cause);
        this.secretId = secretId;
    }

    public String getSecretId() {
        return secretId;
    }
}
