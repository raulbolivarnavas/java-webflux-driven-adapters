package com.raulbolivar.servicename.driven.spexecutor.mapper;

import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureCommandDto;
import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureResultDto;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import com.raulbolivar.servicename.model.StoredProcedureResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StoredProcedureMapper {

    StoredProcedureResult toDomain(StoredProcedureResultDto storedProcedureResultDto);

    StoredProcedureCommandDto toDto(StoredProcedureCommand command);
}
