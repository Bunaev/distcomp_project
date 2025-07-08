package com.discussion.controller;

import com.discussion.dto.ReactionRequestDTO;
import com.discussion.dto.ReactionResponseDTO;
import com.discussion.service.ReactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/v1.0/reactions")
@AllArgsConstructor
@Slf4j
public class ReactionController {

    private final ReactionService reactionService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReactionResponseDTO> getAll() {
        List<ReactionResponseDTO> responseDTO = reactionService.getReactions();
        log.error("Host: {}, method: {}, request: {}, response: {}", 24130, "getAll", null, responseDTO);
        return responseDTO;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReactionResponseDTO create(@RequestBody @Valid ReactionRequestDTO reactionRequestTo) {
        try {
            ReactionResponseDTO responseDTO = reactionService.create(reactionRequestTo);
            log.error("Host: {}, method: {}, request: {}, response: {}", 24130, "create", reactionRequestTo, responseDTO);
            return responseDTO;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ReactionResponseDTO delete(@PathVariable Long id) {
        try {
            ReactionResponseDTO responseDTO = reactionService.deleteReaction(id);
            log.error("Host: {}, method: {}, request: {}, response: {}", 24130, "delete", id, responseDTO);
            return responseDTO;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ReactionResponseDTO update(@RequestBody @Valid ReactionRequestDTO reactionRequestTo) {
        try {
            ReactionResponseDTO responseDTO = reactionService.updateReaction(reactionRequestTo);
            log.error("Host: {}, method: {}, request: {}, response: {}", 24130, "update", reactionRequestTo, responseDTO);
            return responseDTO;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ReactionResponseDTO read(@PathVariable Long id) {
        try {
            ReactionResponseDTO responseDTO = reactionService.get(id);
            log.error("Host: {}, method: {}, request: {}, response: {}", 24130, "update", id, responseDTO);
            return responseDTO;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

}
