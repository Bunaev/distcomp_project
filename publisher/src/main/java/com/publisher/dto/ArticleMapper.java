package com.publisher.dto;


import com.publisher.dto.in.ArticleRequestTo;
import com.publisher.dto.out.ArticleResponseTo;
import com.publisher.entities.Article;
import com.publisher.entities.Creator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    Article toEntity(ArticleRequestTo dto);
    @Mapping(target = "creatorId", expression = "java(getCreatorId(entity.getCreator()))")
    ArticleResponseTo toResponseDto(Article entity);

    default Long getCreatorId(Creator creator) {
        return creator.getId();
    }
}

