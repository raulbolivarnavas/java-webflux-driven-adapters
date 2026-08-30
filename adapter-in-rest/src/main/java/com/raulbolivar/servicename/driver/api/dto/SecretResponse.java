package com.raulbolivar.servicename.driver.api.dto;

import lombok.Builder;

@Builder
public record SecretResponse(
        String name,
        String arn,
        String description,
        String lastChangedDate,
        String lastAccessedDate,
        String createdDate,
        String deletedDate
) {
}
