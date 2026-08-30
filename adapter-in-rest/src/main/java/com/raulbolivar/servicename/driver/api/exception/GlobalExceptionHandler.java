package com.raulbolivar.servicename.driver.api.exception;

import com.raulbolivar.servicename.exception.DatabaseUnavailableException;
import com.raulbolivar.servicename.exception.StoredProcedureExecutionException;
import com.raulbolivar.servicename.exception.StoredProcedureRequestException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.OffsetDateTime;

@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties webProperties,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        setMessageWriters(serverCodecConfigurer.getWriters());
        setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected @Nonnull RouterFunction<ServerResponse> getRoutingFunction(@Nonnull ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);

        if (error instanceof StoredProcedureRequestException exception) {
            return handleRequestException(request, exception);
        }

        if (error instanceof DatabaseUnavailableException exception) {
            return handleDatabaseUnavailable(request, exception);
        }

        if (error instanceof StoredProcedureExecutionException exception) {
            return handleExecutionException(request, exception);
        }

        return handleUnexpectedException(request, error);
    }

    private Mono<ServerResponse> handleRequestException(ServerRequest request,
                                                        StoredProcedureRequestException exception) {
        log.warn("[GLOBAL-ERROR] Invalid request. path={}, message={}",
                request.path(), exception.getMessage());

        ProblemDetail problem = buildProblem(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_STORED_PROCEDURE_REQUEST",
                exception.getMessage()
        );

        return buildResponse(problem);
    }

    private Mono<ServerResponse> handleDatabaseUnavailable(ServerRequest request,
                                                           DatabaseUnavailableException exception) {
        log.error("[GLOBAL-ERROR] Database unavailable. path={}, message={}",
                request.path(),
                exception.getMessage(),
                exception);

        ProblemDetail problem = buildProblem(
                request,
                HttpStatus.SERVICE_UNAVAILABLE,
                "DATABASE_UNAVAILABLE",
                "Base de datos temporalmente no disponible"
        );

        return buildResponse(problem);
    }

    private Mono<ServerResponse> handleExecutionException(ServerRequest request,
                                                          StoredProcedureExecutionException exception) {
        log.error("[GLOBAL-ERROR] Stored procedure execution error. path={}, errorCode={}, sqlState={}, message={}",
                request.path(),
                exception.getErrorCode(),
                exception.getSqlState(),
                exception.getMessage(),
                exception);

        ProblemDetail problem = buildProblem(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "STORED_PROCEDURE_EXECUTION_ERROR",
                "Error ejecutando procedimiento almacenado"
        );

        if (exception.getErrorCode() != null) {
            problem.setProperty("providerErrorCode", exception.getErrorCode());
        }

        if (exception.getSqlState() != null && !exception.getSqlState().isBlank()) {
            problem.setProperty("sqlState", exception.getSqlState());
        }

        return buildResponse(problem);
    }

    private Mono<ServerResponse> handleUnexpectedException(ServerRequest request,
                                                           Throwable exception) {
        log.error("[GLOBAL-ERROR] Unexpected error. path={}, message={}",
                request.path(),
                exception.getMessage(),
                exception);

        ProblemDetail problem = buildProblem(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Ocurrió un error inesperado"
        );

        return buildResponse(problem);
    }

    private ProblemDetail buildProblem(ServerRequest request,
                                       HttpStatus status,
                                       String code,
                                       String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create("https://api.raulbolivar.com/errors/" + code));
        problem.setInstance(URI.create(request.path()));

        problem.setProperty("code", code);
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    private Mono<ServerResponse> buildResponse(ProblemDetail problem) {
        return ServerResponse
                .status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyValue(problem);
    }
}