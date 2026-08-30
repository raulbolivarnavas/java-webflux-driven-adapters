package com.raulbolivar.servicename.driver.api.mapper;

import com.raulbolivar.servicename.driver.api.dto.SecretResponse;
import com.raulbolivar.servicename.driver.api.dto.SecretValueResponse;
import com.raulbolivar.servicename.model.Secret;
import com.raulbolivar.servicename.model.SecretValue;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SecretsMapper {

    SecretResponse toSecretResponse(Secret secret);

    SecretValueResponse toSecretValueResponse(SecretValue secretValue);
}
