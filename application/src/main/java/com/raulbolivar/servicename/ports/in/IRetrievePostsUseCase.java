package com.raulbolivar.servicename.ports.in;

import com.raulbolivar.servicename.model.Post;
import reactor.core.publisher.Flux;

public interface IRetrievePostsUseCase {
    Flux<Post> retrieveAll();
}
