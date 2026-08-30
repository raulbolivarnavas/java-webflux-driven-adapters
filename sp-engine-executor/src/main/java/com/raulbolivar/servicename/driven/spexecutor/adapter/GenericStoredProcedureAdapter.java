package com.raulbolivar.servicename.driven.spexecutor.adapter;

import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureCommandDto;
import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureResultDto;
import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureResultSetDto;
import com.raulbolivar.servicename.driven.spexecutor.helper.SqlParameterParser;
import com.raulbolivar.servicename.driven.spexecutor.mapper.ProcedureResponseMapper;
import com.raulbolivar.servicename.driven.spexecutor.mapper.StoredProcedureMapper;
import com.raulbolivar.servicename.exception.DatabaseUnavailableException;
import com.raulbolivar.servicename.exception.StoredProcedureExecutionException;
import com.raulbolivar.servicename.exception.StoredProcedureRequestException;
import com.raulbolivar.servicename.model.ProcedureResponse;
import com.raulbolivar.servicename.model.ProcedureResultDefinition;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import com.raulbolivar.servicename.ports.out.StoredProcedureExecutorGateway;
import io.r2dbc.spi.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenericStoredProcedureAdapter implements StoredProcedureExecutorGateway {

    private final ConnectionFactory       connectionFactory;
    private final StoredProcedureMapper   mapper;
    private final ProcedureResponseMapper responseMapper;

    @Override
    public Mono<ProcedureResponse> execute(StoredProcedureCommand command,
                                           String spContent,
                                           ProcedureResultDefinition definition) {
        return Mono.usingWhen(
                        connectionFactory.create(),
                        connection -> executeCommand(
                                connection,
                                mapper.toDto(command),
                                spContent
                        ),
                        Connection::close
                )
                .map(result -> responseMapper.map(result, definition))
                .onErrorMap(this::mapException);
    }

    private Mono<StoredProcedureResultDto> executeCommand(Connection connection,
                                                          StoredProcedureCommandDto command,
                                                          String spContent) {
        return Mono.defer(() -> {
            SqlParameterParser.ParsedSql parsed = SqlParameterParser
                    .parse(spContent, command.parameters());

            log.debug("[SP-EXECUTOR] Executing SQL with {} parameters",
                    parsed.parameters().size());

            Statement statement = connection.createStatement(parsed.sql());
            bindParameters(statement, parsed);

            return Flux.from(statement.execute())
                    .concatMap(this::mapResult)
                    .filter(rows -> !rows.isEmpty())
                    .index()
                    .map(tuple -> new StoredProcedureResultSetDto(
                            tuple.getT1().intValue(),
                            tuple.getT2()
                    ))
                    .collectList()
                    .map(StoredProcedureResultDto::new);
        });
    }

    private void bindParameters(Statement statement,
                                SqlParameterParser.ParsedSql parsed) {
        parsed.parameters().forEach(parameter -> {
            Object value = parameter.value();

            log.debug("[SP-EXECUTOR] Binding SQL parameter name={}",
                    parameter.name());

            if (value == null) {
                statement.bindNull(parameter.name(), String.class);
            } else {
                statement.bind(parameter.name(), value);
            }
        });
    }

    private Mono<List<Map<String, Object>>> mapResult(Result result) {
        return Flux.from(result.map(this::mapRow)).collectList();
    }

    private Map<String, Object> mapRow(Row row,
                                       RowMetadata metadata) {
        Map<String, Object> values = new LinkedHashMap<>();

        metadata.getColumnMetadatas()
                .forEach(column -> {
                    String columnName = column.getName();
                    values.put(columnName, row.get(columnName));
                });

        return values;
    }

    private Throwable mapException(Throwable throwable) {
        if (throwable instanceof StoredProcedureExecutionException) {
            return throwable;
        }

        if (throwable instanceof R2dbcTimeoutException exception) {
            log.error("[SP-EXECUTOR] Database timeout. sqlState={}, errorCode={}, message={}",
                    exception.getSqlState(),
                    exception.getErrorCode(),
                    exception.getMessage(),
                    exception);

            return new DatabaseUnavailableException("Timeout ejecutando operación en base de datos",
                    exception);
        }

        if (throwable instanceof R2dbcTransientResourceException exception) {
            log.error("[SP-EXECUTOR] Database temporarily unavailable. sqlState={}, errorCode={}, message={}",
                    exception.getSqlState(),
                    exception.getErrorCode(),
                    exception.getMessage(),
                    exception);

            return new DatabaseUnavailableException("Base de datos temporalmente no disponible",
                    exception);
        }

        if (throwable instanceof R2dbcException exception) {
            log.error("[SP-EXECUTOR] SQL execution error. sqlState={}, errorCode={}, message={}",
                    exception.getSqlState(),
                    exception.getErrorCode(),
                    exception.getMessage(),
                    exception);

            return new StoredProcedureExecutionException(
                    exception.getErrorCode(),
                    exception.getSqlState(),
                    exception.getMessage(),
                    exception
            );
        }

        if (throwable instanceof IllegalArgumentException exception) {
            return new StoredProcedureRequestException(exception.getMessage(), exception);
        }

        return new StoredProcedureExecutionException(
                null,
                null,
                "Error inesperado ejecutando procedimiento almacenado",
                throwable
        );
    }
}