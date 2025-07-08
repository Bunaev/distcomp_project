package com.publisher.controller;

import com.publisher.dto.in.StickerRequestTo;
import com.publisher.dto.out.StickerResponseTo;
import com.publisher.service.StickerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/v1.0/stickers")
@AllArgsConstructor
public class StickerController {
    private final StickerService stickerService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<StickerResponseTo> getAll() {
        return stickerService.getStickers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StickerResponseTo create(@RequestBody @Valid StickerRequestTo stickerRequestTo) {
        try {
            return stickerService.create(stickerRequestTo);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public StickerResponseTo delete(@PathVariable Long id) {
        try {
            return stickerService.deleteSticker(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public StickerResponseTo update(@RequestBody @Valid StickerRequestTo stickerRequestTo) {
        try {
            return stickerService.updateSticker(stickerRequestTo);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public StickerResponseTo read(@PathVariable Long id) {
        try {
            return stickerService.get(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
