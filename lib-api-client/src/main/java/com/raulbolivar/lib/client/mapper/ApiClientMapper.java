package com.raulbolivar.lib.client.mapper;

import com.raulbolivar.lib.client.dto.ApiRequestDto;
import com.raulbolivar.lib.client.dto.ApiResponseDto;
import com.raulbolivar.servicename.model.ApiRequest;
import com.raulbolivar.servicename.model.ApiResponse;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiClientMapper {

    ApiRequestDto toApiRequestDto(ApiRequest request);

    //<T> ApiResponse<T> toApiResponse(ApiResponseDto<T> tApiResponseDto);
    default <T> ApiResponse<T> toApiResponse(ApiResponseDto<T> tApiResponseDto) {
        if (tApiResponseDto == null) {
            return null;
        }

        return new ApiResponse<>(
                tApiResponseDto.status(),
                tApiResponseDto.headers(),
                tApiResponseDto.body()
        );
    }
}
