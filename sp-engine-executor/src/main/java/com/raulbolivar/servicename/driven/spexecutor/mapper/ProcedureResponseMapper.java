package com.raulbolivar.servicename.driven.spexecutor.mapper;

import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureResultDto;
import com.raulbolivar.servicename.model.ProcedureResponse;
import com.raulbolivar.servicename.model.ProcedureResultDefinition;
import com.raulbolivar.servicename.model.ProcedureStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ProcedureResponseMapper {

    public ProcedureResponse map(StoredProcedureResultDto source,
                                 ProcedureResultDefinition definition) {
        List<Map<String, Object>> data = new ArrayList<>();
        ProcedureStatus status = null;

        for (var resultSet : source.resultSets()) {
            for (Map<String, Object> row : resultSet.rows()) {
                if (isStatusRow(row, definition)) {
                    status = mapStatus(row, definition);
                } else {
                    data.add(row);
                }
            }
        }

        if (status == null) {
            status = new ProcedureStatus(true, null, null);
        }

        return new ProcedureResponse(data, status);
    }

    private boolean isStatusRow(Map<String, Object> row,
                                ProcedureResultDefinition definition) {
        return containsKeyIgnoreCase(row, definition.codeField())
                || containsKeyIgnoreCase(row, definition.messageField())
                || containsKeyIgnoreCase(row, definition.returnField());
    }

    private ProcedureStatus mapStatus(Map<String, Object> row,
                                      ProcedureResultDefinition definition) {
        Object codeValue = getIgnoreCase(row, definition.codeField());
        Object messageValue = getIgnoreCase(row, definition.messageField());

        String code = codeValue != null
                ? String.valueOf(codeValue)
                : null;

        String message = messageValue != null
                ? String.valueOf(messageValue)
                : null;

        boolean success = code == null
                || definition.successCode().equals(code);

        return new ProcedureStatus(success, code, message);
    }

    private Object getIgnoreCase(Map<String, Object> row,
                                 String key) {
        if (row == null || key == null) {
            return null;
        }

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null
                    && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private boolean containsKeyIgnoreCase(Map<String, Object> row,
                                          String key) {
        if (row == null || key == null) {
            return false;
        }

        return row.keySet()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(column -> column.equalsIgnoreCase(key));
    }
}