package com.raulbolivar.driven.secretsmanager.mapper;

import com.raulbolivar.servicename.exception.SecretAccessException;
import com.raulbolivar.servicename.exception.SecretMappingException;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class SecretValueMapper {

    private final JsonMapper jsonMapper;

    public SecretValueMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String extractValue(
            String secretId,
            GetSecretValueResponse response) {

        if (response.secretString() != null) {
            return response.secretString();
        }

        if (response.secretBinary() != null) {
            return new String(
                    response.secretBinary().asByteArray(),
                    StandardCharsets.UTF_8
            );
        }

        throw new SecretAccessException(
                secretId,
                "AWS Secrets Manager devolvió un secreto sin contenido",
                null
        );
    }

    public Map<String, String> toMap(
            String secretId,
            String secretValue) {

        try {
            return jsonMapper.readValue(
                    secretValue,
                    new TypeReference<Map<String, String>>() {
                    }
            );
        } catch (Exception exception) {
            throw new SecretMappingException(
                    secretId,
                    Map.class,
                    exception
            );
        }
    }

    public <T> T toObject(
            String secretId,
            String secretValue,
            Class<T> targetType) {

        try {
            return jsonMapper.readValue(secretValue, targetType);
        } catch (Exception exception) {
            throw new SecretMappingException(
                    secretId,
                    targetType,
                    exception
            );
        }
    }
}