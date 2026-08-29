package com.raulbolivar.servicename.driver.api.mapper;

import com.raulbolivar.servicename.driver.api.dto.StoredProcedureRequestDto;
import com.raulbolivar.servicename.model.StoredProcedureCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpExecutorMapper {

    StoredProcedureCommand toDomain(StoredProcedureRequestDto storedProcedureRequestDto);
}
