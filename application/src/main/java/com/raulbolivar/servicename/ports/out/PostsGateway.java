package com.raulbolivar.servicename.ports.out;

import com.raulbolivar.servicename.model.Post;
import reactor.core.publisher.Flux;

public interface PostsGateway {
    Flux<Post> retrieveAll();
}
