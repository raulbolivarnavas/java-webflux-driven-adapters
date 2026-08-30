package com.raulbolivar.servicename.driven.spexecutor.mapper;

import com.raulbolivar.servicename.driven.spexecutor.dto.StoredProcedureCommandDto;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StoredProcedureMapper {

    StoredProcedureCommandDto toDto(StoredProcedureCommand command);
}
