package com.publisher.service;

import com.publisher.dto.StickerMapper;
import com.publisher.dto.in.StickerRequestTo;
import com.publisher.dto.out.StickerResponseTo;
import com.publisher.entities.Sticker;
import com.publisher.repository.StickerRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
@Transactional
public class StickerService {
    private final StickerRepository stickerRepository;
    private StickerMapper mapper;

    @CacheEvict(value = "allStickers", allEntries = true)
    public StickerResponseTo create(StickerRequestTo stickerRequestTo) {
        Sticker sticker = Sticker.builder()
                .name(stickerRequestTo.getName()).build();
        return mapper.toResponseDto(stickerRepository.save(sticker));
    }
    @Cacheable(value = "allStickers")
    public List<StickerResponseTo> getStickers() {
        return stickerRepository.findAll()
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }
    @CacheEvict(value = "stickers", key = "#id")
    public StickerResponseTo deleteSticker(Long id) {
        Sticker sticker = stickerRepository.findById(id).orElseThrow();
        stickerRepository.delete(sticker);
        return mapper.toResponseDto(sticker);
    }
    @CachePut(value = "stickers", key = "#stickerRequestTo.id")
    @CacheEvict(value = "allStickers", allEntries = true)
    public StickerResponseTo updateSticker(StickerRequestTo stickerRequestTo) {
        Sticker stickerToUpdate = stickerRepository.findById(stickerRequestTo.getId()).orElseThrow();
        stickerToUpdate.setName(stickerRequestTo.getName());
        return mapper.toResponseDto(stickerRepository.save(stickerToUpdate));
    }
    @Cacheable(value = "stickers", key = "#id")
    public StickerResponseTo get(Long id) {
        return mapper.toResponseDto(stickerRepository.findById(id).orElseThrow());
    }

}
