package com.publisher.dto;

import com.publisher.dto.in.CreatorRequestTo;
import com.publisher.dto.out.CreatorResponseTo;
import com.publisher.entities.Creator;

@org.mapstruct.Mapper(componentModel = "spring")
public interface CreatorMapper {

    Creator toEntity(CreatorRequestTo dto);
    CreatorResponseTo toResponseDto(Creator entity);

}

