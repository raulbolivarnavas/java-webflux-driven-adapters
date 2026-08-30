package com.raulbolivar.lib.client.adapter;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.raulbolivar.lib.client.config.GenericApiClientProperties;
import com.raulbolivar.lib.client.dto.ApiRequestDto;
import com.raulbolivar.lib.client.dto.ApiResponseDto;
import com.raulbolivar.lib.client.mapper.ApiClientMapper;
import com.raulbolivar.servicename.exception.*;
import com.raulbolivar.servicename.model.ApiRequest;
import com.raulbolivar.servicename.model.ApiResponse;
import com.raulbolivar.servicename.ports.out.GenericApiClientGateway;
import com.raulbolivar.servicename.ports.out.GenericApiResilienceExecutorGateway;

import io.github.raulbolivarnavas.supportlogging.SupportLogging;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogCapture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

@Component
public class WebClientGenericApiClient implements GenericApiClientGateway {

    private final WebClient                           webClient;
    private final GenericApiClientProperties          properties;
    private final GenericApiResilienceExecutorGateway resilience;
    private final JsonMapper                          jsonMapper;
    private final ApiClientMapper                     mapper;
    private final SupportLogCapture                   supportLogCapture;

    public WebClientGenericApiClient(WebClient webClient,
                                     GenericApiClientProperties properties,
                                     GenericApiResilienceExecutorGateway resilience,
                                     JsonMapper jsonMapper,
                                     ApiClientMapper mapper,
                                     SupportLogCapture supportLogCapture) {
        this.webClient = webClient;
        this.properties = properties;
        this.resilience = resilience;
        this.jsonMapper = jsonMapper;
        this.mapper = mapper;
        this.supportLogCapture = supportLogCapture;
    }

    @Override
    @SupportLogging(operation = "processing")
    public <T> Mono<ApiResponse<T>> execute(ApiRequest request, Class<T> responseType) {
        var endpoint = endpoint(request.operation());
        return supportLogCapture
                .request(
                        endpoint.method().name(),
                        endpoint.url(),
                        request.queryParams(),
                        request.headers(),
                        request.body()
                )
                .then(resilience
                        .execute(request.operation(), invoke(mapper.toApiRequestDto(request), endpoint, responseType))
                        .map(mapper::toApiResponse)
                );
    }

    private <T> Mono<ApiResponseDto<T>> invoke(ApiRequestDto request,
                                               GenericApiClientProperties.Endpoint endpoint,
                                               Class<T> responseType) {
        URI uri = UriComponentsBuilder.fromUriString(endpoint.url())
                .queryParams(toQueryParams(request.queryParams()))
                .buildAndExpand(request.pathParams())
                .toUri();

        var spec = webClient.method(HttpMethod.valueOf(endpoint.method().name()))
                .uri(uri)
                .headers(headers -> mergeHeaders(headers, endpoint, request));

        var requestSpec = request.body() == null
                ? spec.body(BodyInserters.empty())
                : spec.bodyValue(request.body());

        return requestSpec
                .exchangeToMono(response -> decode(response, request.operation(), responseType))
                .onErrorMap(WebClientRequestException.class,error -> new ExternalConnectionException(request.operation(), error));
    }

    private <T> Mono<ApiResponseDto<T>> decode(
            ClientResponse response, String operation, Class<T> responseType
    ) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> {
                    int status = response.statusCode().value();
                    if (status >= 400 && status < 500) {
                        return Mono.error(new ExternalClientException(operation, status, body));
                    }
                    if (status >= 500) {
                        return Mono.error(new ExternalServerException(operation, status, body));
                    }
                    return Mono.just(new ApiResponseDto<>(status, responseHeaders(response),
                            deserialize(operation, body, responseType)));
                });
    }

    private <T> T deserialize(String operation, String body, Class<T> responseType) {
        if (responseType == Void.class || body == null || body.isBlank()) return null;
        try {
            return jsonMapper.readValue(body, responseType);
        } catch (Exception error) {
            throw new ExternalResponseMappingException(operation, responseType, error);
        }
    }

    private GenericApiClientProperties.Endpoint endpoint(String operation) {
        var endpoint = properties.endpoints().get(operation);
        if (endpoint == null) throw new OperationNotConfiguredException(operation);
        return endpoint;
    }

    private void mergeHeaders(HttpHeaders target,
                              GenericApiClientProperties.Endpoint endpoint,
                              ApiRequestDto request) {
        if (properties.defaultHeaders() != null) properties.defaultHeaders().forEach(target::set);
        if (endpoint.headers() != null) endpoint.headers().forEach(target::set);
        request.headers().forEach(target::set);
        target.set("Correlation-Id", request.correlationId());
    }

    private org.springframework.util.MultiValueMap<String, String> toQueryParams(
            Map<String, String> source) {
        var result = new org.springframework.util.LinkedMultiValueMap<String, String>();
        source.forEach(result::add);
        return result;
    }

    private Map<String, List<String>> responseHeaders(ClientResponse response) {
        var headers = new LinkedHashMap<String, List<String>>();
        response.headers().asHttpHeaders()
                .forEach((name, values) -> headers.put(name, List.copyOf(values)));
        return Map.copyOf(headers);
    }
}