package com.raulbolivar.servicename.model;

public record AwsConnectionValue(
        String accessKeyId,
        String secretAccessKey,
        String sessionToken,
        String region
) {
}
