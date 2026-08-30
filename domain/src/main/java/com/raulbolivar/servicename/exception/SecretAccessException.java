package com.raulbolivar.servicename.exception;

public class SecretAccessException extends RuntimeException {

    private final String secretId;

    public SecretAccessException(
            String secretId,
            String message,
            Throwable cause) {

        super(message, cause);
        this.secretId = secretId;
    }

    public String getSecretId() {
        return secretId;
    }
}
