package com.raulbolivar.driven.jsonplaceholder.adapter;

import com.raulbolivar.driven.jsonplaceholder.dto.PostResponseDto;
import com.raulbolivar.driven.jsonplaceholder.mapper.PostResponseMapper;
import com.raulbolivar.servicename.model.ApiRequest;
import com.raulbolivar.servicename.model.ApiResponse;
import com.raulbolivar.servicename.model.Post;
import com.raulbolivar.servicename.ports.out.GenericApiClientGateway;
import com.raulbolivar.servicename.ports.out.PostsGateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class JsonPlaceholderPostsAdapter implements PostsGateway {

    private static final String OPERATION = "json-placeholder-retrieve-posts";

    private final GenericApiClientGateway genericApiClient;
    private final PostResponseMapper      mapper;

    @Override
    public Flux<Post> retrieveAll() {
        return genericApiClient.execute(
                        ApiRequest.builder().operation(OPERATION).build(),
                        PostResponseDto[].class
                )
                .map(ApiResponse::body)
                .flatMapMany(this::toFlux);
    }

    private Flux<Post> toFlux(PostResponseDto[] response) {
        if (response == null || response.length == 0) {
            return Flux.empty();
        }

        return Flux.fromArray(response)
                .map(mapper::toDomain);
    }
}
