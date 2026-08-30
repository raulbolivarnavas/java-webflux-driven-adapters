package com.raulbolivar.servicename.driver.api;

import com.raulbolivar.servicename.driver.api.handler.RetrievePostsHandler;
import com.raulbolivar.servicename.driver.api.handler.SecretsHandler;
import com.raulbolivar.servicename.driver.api.handler.SpExecutorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    public RouterFunction<ServerResponse> storedProcedureRoutes(
            SpExecutorHandler spExecutorHandler,
            RetrievePostsHandler retrievePostsHandler,
            SecretsHandler secretsHandler
    ) {
        return route()
                .POST("/api/v1/stored-procedures/execute",
                        accept(MediaType.APPLICATION_JSON)
                                .and(contentType(MediaType.APPLICATION_JSON)),
                        spExecutorHandler::execute)
                .GET("/api/v1/posts",
                        accept(MediaType.APPLICATION_JSON),
                        retrievePostsHandler::retrieveAll)
                .GET("/api/v1/secrets",
                        accept(MediaType.APPLICATION_JSON),
                        secretsHandler::getSecretValue)
                .GET("/api/v1/database-secrets",
                        accept(MediaType.APPLICATION_JSON),
                        secretsHandler::getDatabaseSecret)
                .build();
    }
}
