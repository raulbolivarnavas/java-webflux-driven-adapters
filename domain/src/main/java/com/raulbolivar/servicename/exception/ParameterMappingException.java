package com.raulbolivar.servicename.exception;

public class ParameterMappingException
        extends RuntimeException {

    private final String parameterName;
    private final Class<?> targetType;

    public ParameterMappingException(
            String parameterName,
            Class<?> targetType,
            Throwable cause) {

        super(
                "No fue posible convertir el parámetro '%s' al tipo '%s'"
                        .formatted(
                                parameterName,
                                targetType.getSimpleName()
                        ),
                cause
        );

        this.parameterName = parameterName;
        this.targetType = targetType;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Class<?> getTargetType() {
        return targetType;
    }
}