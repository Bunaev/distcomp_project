package com.discussion.service;

import com.discussion.dto.ReactionMapper;
import com.discussion.dto.ReactionRequestDTO;
import com.discussion.dto.ReactionResponseDTO;
import com.discussion.entities.Reaction;
import com.discussion.repository.ReactionRepoCassandra;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@AllArgsConstructor
@Transactional
public class ReactionService {
    private final ReactionRepoCassandra reactionRepository;
    private ReactionMapper mapper;
    private final AtomicLong counter = new AtomicLong();

    public ReactionResponseDTO create(ReactionRequestDTO reactionRequestTo) {
        Reaction reaction = mapper.toEntity(reactionRequestTo);
        reaction.setId(counter.incrementAndGet());
        reaction.setArticleId(reactionRequestTo.getArticleId());
        return mapper.toResponseDto(reactionRepository.save(reaction));
    }

    public List<ReactionResponseDTO> getReactions() {
        return reactionRepository.findAll()
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    public ReactionResponseDTO deleteReaction(Long id) {
        Reaction reaction = reactionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reaction not found with id: " + id));
        reactionRepository.delete(reaction);
        return mapper.toResponseDto(reaction);
    }

    public ReactionResponseDTO updateReaction(ReactionRequestDTO reactionRequestTo) {
        Reaction reaction = reactionRepository.findById(reactionRequestTo.getId()).orElseThrow();
        reaction.setContent(reactionRequestTo.getContent());
        reaction.setArticleId(reactionRequestTo.getArticleId());
        return mapper.toResponseDto(reactionRepository.save(reaction));
    }

    public ReactionResponseDTO get(Long id) {
        return mapper.toResponseDto(reactionRepository.findById(id).orElseThrow());
    }
}
