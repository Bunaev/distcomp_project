package com.publisher.service;

import com.publisher.config.LoginAlreadyExistException;
import com.publisher.dto.CreatorMapper;
import com.publisher.dto.in.CreatorRequestTo;
import com.publisher.dto.out.CreatorResponseTo;
import com.publisher.entities.Creator;
import com.publisher.entities.Role;
import com.publisher.repository.CreatorRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Transactional
@Service
public class CreatorService {
    private final CreatorRepository creatorRepository;
    private CreatorMapper mapper;
    private final PasswordEncoder passwordEncoder;


    @CacheEvict(value = "allCreators", allEntries = true)
    public CreatorResponseTo create(CreatorRequestTo creatorRequestTo) {
        if (creatorRepository.findByLogin(creatorRequestTo.getLogin()).isPresent()) {
            throw new LoginAlreadyExistException("Login is already exist!");
        } else {
            Creator creator = mapper.toEntity(creatorRequestTo);
            creator.setRole(creatorRequestTo.getRole() == null?Role.CUSTOMER: Role.valueOf(creatorRequestTo.getRole()));
            creator.setPassword(passwordEncoder.encode(creatorRequestTo.getPassword()));
            return mapper.toResponseDto(creatorRepository.save(creator));
        }
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

    public boolean existsByLogin(String login) {
        return creatorRepository.findByLogin(login).isPresent();
    }
}
