package com.raulbolivar.servicename.model;

public record Post(
        Long userId,
        Long id,
        String title,
        String body
) {
}
