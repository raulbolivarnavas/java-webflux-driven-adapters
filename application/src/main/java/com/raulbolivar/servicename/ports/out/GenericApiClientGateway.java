package com.raulbolivar.servicename.ports.out;

import com.raulbolivar.servicename.model.ApiRequest;
import com.raulbolivar.servicename.model.ApiResponse;
import reactor.core.publisher.Mono;

public interface GenericApiClientGateway {
    <T> Mono<ApiResponse<T>> execute(ApiRequest request, Class<T> responseType);
}
