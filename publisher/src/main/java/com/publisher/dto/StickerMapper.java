package com.publisher.dto;

import com.publisher.dto.in.StickerRequestTo;
import com.publisher.dto.out.StickerResponseTo;
import com.publisher.entities.Sticker;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StickerMapper {
    Sticker toEntity(StickerRequestTo dto);
    StickerResponseTo toResponseDto(Sticker entity);
}
