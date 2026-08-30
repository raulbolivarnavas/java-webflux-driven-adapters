package com.raulbolivar.servicename.exception;

public class SecretMappingException extends RuntimeException {

    private final String secretId;
    private final Class<?> targetType;

    public SecretMappingException(
            String secretId,
            Class<?> targetType,
            Throwable cause) {

        super(
                "No fue posible convertir el secreto '%s' al tipo '%s'"
                        .formatted(secretId, targetType.getSimpleName()),
                cause
        );

        this.secretId = secretId;
        this.targetType = targetType;
    }

    public String getSecretId() {
        return secretId;
    }

    public Class<?> getTargetType() {
        return targetType;
    }
}
