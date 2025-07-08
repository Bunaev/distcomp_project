package com.publisher.controller;

import com.publisher.dto.in.ArticleRequestTo;
import com.publisher.dto.out.ArticleResponseTo;
import com.publisher.service.ArticleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/v1.0/articles")
@AllArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ArticleResponseTo> getAll() {
        return articleService.getArticles();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponseTo create(@RequestBody @Valid ArticleRequestTo articleRequestTo) {
        try {
            return articleService.create(articleRequestTo);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ArticleResponseTo delete(@PathVariable Long id) {
        try {
            return articleService.deleteArticle(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ArticleResponseTo update(@RequestBody @Valid ArticleRequestTo articleRequestTo) {
        try {
            return articleService.updateArticle(articleRequestTo);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }

    @GetMapping("/{id}")
    public ArticleResponseTo read(@PathVariable Long id) {
        try {
            return articleService.get(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
