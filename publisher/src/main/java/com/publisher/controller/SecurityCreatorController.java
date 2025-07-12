package com.publisher.controller;

import com.publisher.dto.in.CreatorRequestTo;
import com.publisher.dto.out.CreatorResponseTo;
import com.publisher.service.CreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("api/v2.0/creators")
@AllArgsConstructor
public class SecurityCreatorController {
    private final CreatorService creatorService;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(creatorService.getCreators());
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CreatorResponseTo delete(@PathVariable Long id) {
        try {
            return creatorService.deleteCreator(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public CreatorResponseTo update(@RequestBody @Valid CreatorRequestTo creatorRequestTo) {
        try {
            return creatorService.updateCreator(creatorRequestTo);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public CreatorResponseTo read(@PathVariable Long id) {
        try {
            return creatorService.get(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
