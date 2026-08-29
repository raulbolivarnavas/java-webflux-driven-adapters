package com.raulbolivar.servicename.driven.spexecutor.adapter;

import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureCommandDto;
import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureResultDto;
import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureResultSetDto;
import com.raulbolivar.servicename.driven.spexecutor.helper.SqlParameterParser;
import com.raulbolivar.servicename.driven.spexecutor.mapper.StoredProcedureMapper;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import com.raulbolivar.servicename.model.StoredProcedureResult;
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

    private final ConnectionFactory connectionFactory;
    private final StoredProcedureMapper mapper;

    @Override
    public Mono<StoredProcedureResult> execute(StoredProcedureCommand command,
                                               String spContent) {
        return Mono
                .usingWhen(connectionFactory.create(),connection ->
                        executeCommand(connection, mapper.toDto(command), spContent),
                        Connection::close
                ).map(mapper::toDomain);
    }

    private Mono<StoredProcedureResultDto> executeCommand(Connection connection,
                                                          StoredProcedureCommandDto command,
                                                          String spContent) {
        SqlParameterParser.ParsedSql parsed = SqlParameterParser
                .parse(spContent, command.parameters());

        log.debug("Executing SQL with {} parameters", parsed.parameters().size());

        Statement statement = connection.createStatement(parsed.sql());
        bindParameters(statement, parsed);

        return Flux.from(statement.execute())
                .concatMap(this::mapResult)
                .filter(rows -> !rows.isEmpty())
                .index()
                .map(tuple ->
                        new StoredProcedureResultSetDto(
                                tuple.getT1().intValue(),
                                tuple.getT2()
                        )
                )
                .collectList()
                .map(StoredProcedureResultDto::new);
    }

    private void bindParameters(Statement statement,
                                SqlParameterParser.ParsedSql parsed) {
        parsed.parameters().forEach(parameter -> {
            Object value = parameter.value();
            log.debug("Binding SQL parameter name={}", parameter.name());

            if (value == null) {
                statement.bindNull(parameter.name(), String.class);
            } else {
                statement.bind(parameter.name(), value);
            }
        });
    }

    private Mono<List<Map<String, Object>>> mapResult(Result result) {
        return Flux.from(result.map(this::mapRow))
                .collectList();
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
}