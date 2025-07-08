package com.publisher.service;

import com.publisher.dto.CreatorMapper;
import com.publisher.dto.in.CreatorRequestTo;
import com.publisher.dto.out.CreatorResponseTo;
import com.publisher.entities.Creator;
import com.publisher.repository.CreatorRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Transactional
@Service
public class CreatorService {
    private final CreatorRepository creatorRepository;
    private CreatorMapper mapper;

    @CacheEvict(value = "allCreators", allEntries = true)
    public CreatorResponseTo create(CreatorRequestTo creatorRequestTo) {
        Creator creator = Creator.builder()
                .login(creatorRequestTo.getLogin())
                .firstname(creatorRequestTo.getFirstname())
                .password(creatorRequestTo.getPassword())
                .lastname(creatorRequestTo.getLastname()).build();
        return mapper.toResponseDto(creatorRepository.save(creator));
    }
    @Cacheable(value = "allCreators")
    public List<CreatorResponseTo> getCreators() {
        return creatorRepository.findAll()
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }
    @CacheEvict(value = "creators", key = "#id")
    public CreatorResponseTo deleteCreator(Long id) {
        Creator creator = creatorRepository.findById(id).orElseThrow();
        creatorRepository.delete(creator);
        return mapper.toResponseDto(creator);
    }
    @CachePut(value = "creators", key = "#creatorRequestTo.id")
    @CacheEvict(value = "allCreators", allEntries = true)
    public CreatorResponseTo updateCreator(CreatorRequestTo creatorRequestTo) {
        Creator creatorToUpdate = creatorRepository.findById(creatorRequestTo.getId()).orElseThrow();
        creatorToUpdate.setFirstname(creatorRequestTo.getFirstname());
        creatorToUpdate.setLastname(creatorRequestTo.getLastname());
        creatorToUpdate.setLogin(creatorRequestTo.getLogin());
        creatorToUpdate.setPassword(creatorRequestTo.getPassword());
        return mapper.toResponseDto(creatorRepository.save(creatorToUpdate));
    }
    @Cacheable(value = "creators", key = "#id")
    public CreatorResponseTo get(Long id) {
        return mapper.toResponseDto(creatorRepository.findById(id).orElseThrow());
    }
}
