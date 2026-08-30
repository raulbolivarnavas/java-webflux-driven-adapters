package com.raulbolivar.driven.jsonplaceholder.mapper;

import com.raulbolivar.driven.jsonplaceholder.dto.PostResponseDto;
import com.raulbolivar.servicename.model.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostResponseMapper {

    Post toDomain(PostResponseDto response);
}
