package com.publisher.controller;

import com.publisher.service.CommunicationKafkaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("api/v1.0/reactions")
@AllArgsConstructor
@Slf4j
public class ReactionController {
    private final CommunicationKafkaService kafkaService;
    private static final long KAFKA_RESPONSE_TIMEOUT = 2000;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Object getAll() {
        try {
            CompletableFuture<Object> future = kafkaService.send(null, RequestMethod.GET);
            return future.get(KAFKA_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting reactions");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Object createReaction(@RequestBody Map<String, Object> reactionRequest) {
        try {
            CompletableFuture<Object> future = kafkaService.send(reactionRequest, RequestMethod.POST);
            return future.get(KAFKA_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating reaction");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = "reaction", key = "#id")
    public Object delete(@PathVariable Long id) {
        try {
            CompletableFuture<Object> future = kafkaService.send(Map.of("id", id), RequestMethod.DELETE);
            return future.get(KAFKA_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reaction not found");
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @CachePut(value = "reaction", key = "#reactionRequest.get('id')")
    public Object update(@RequestBody Map<String, Object> reactionRequest) {
        try {
            CompletableFuture<Object> future = kafkaService.send(reactionRequest, RequestMethod.PUT);
            return future.get(KAFKA_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reaction not found");
        }
    }

    @GetMapping("/{id}")
    @Cacheable(value = "reaction", key = "#id")
    public Object read(@PathVariable Long id) {
        try {
            CompletableFuture<Object> future = kafkaService.send(Map.of("id", id), RequestMethod.GET);
            return future.get(KAFKA_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reaction not found");
        }
    }
}
