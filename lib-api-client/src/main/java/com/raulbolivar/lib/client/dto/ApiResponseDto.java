package com.raulbolivar.lib.client.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ApiResponseDto<T>(
        int status,
        Map<String, List<String>> headers,
        T body
) {

    public ApiResponseDto {
        var copy = new LinkedHashMap<String, List<String>>();

        if (headers != null) {
            headers.forEach((key, values) -> copy.put(key, List.copyOf(values)));
        }

        headers = Map.copyOf(copy);
    }

    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }
}
