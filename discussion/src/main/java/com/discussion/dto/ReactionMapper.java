package com.discussion.dto;

import com.discussion.entities.Reaction;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ReactionMapper {

    Reaction toEntity(ReactionRequestDTO dto);

    ReactionResponseDTO toResponseDto(Reaction entity);

}
