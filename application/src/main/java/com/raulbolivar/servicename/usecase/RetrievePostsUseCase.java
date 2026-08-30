package com.raulbolivar.servicename.usecase;

import com.raulbolivar.servicename.model.Post;
import com.raulbolivar.servicename.ports.in.IRetrievePostsUseCase;
import com.raulbolivar.servicename.ports.out.PostsGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class RetrievePostsUseCase implements IRetrievePostsUseCase {

    private final PostsGateway postsGateway;

    @Override
    public Flux<Post> retrieveAll() {
        return postsGateway.retrieveAll();
    }
}
