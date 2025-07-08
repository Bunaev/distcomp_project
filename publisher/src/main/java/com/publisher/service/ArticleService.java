package com.publisher.service;

import com.publisher.dto.ArticleMapper;
import com.publisher.dto.in.ArticleRequestTo;
import com.publisher.dto.out.ArticleResponseTo;
import com.publisher.entities.Article;
import com.publisher.entities.Creator;
import com.publisher.repository.ArticleRepository;
import com.publisher.repository.CreatorRepository;
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
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final CreatorRepository creatorRepository;
    private ArticleMapper mapper;

    @CacheEvict(value = "allArticles", allEntries = true)
    public ArticleResponseTo create(ArticleRequestTo articleRequestTo) {
        Creator creator = creatorRepository.findById(articleRequestTo.getCreatorId()).orElseThrow();
        Article article = Article.builder()
                .creator(creator)
                .title(articleRequestTo.getTitle())
                .content(articleRequestTo.getContent()).build();
        return mapper.toResponseDto(articleRepository.save(article));
    }
    @Cacheable(value = "allArticles")
    public List<ArticleResponseTo> getArticles() {
        return articleRepository.findAll()
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }
    @CacheEvict(value = "articles", key = "#id")
    public ArticleResponseTo deleteArticle(Long id) {
        Article article = articleRepository.findById(id).orElseThrow();
        articleRepository.delete(article);
        return mapper.toResponseDto(article);
    }
    @CachePut(value = "articles", key = "#articleRequestTo.id")
    @CacheEvict(value = "allArticles", allEntries = true)
    public ArticleResponseTo updateArticle(ArticleRequestTo articleRequestTo) {
        Article articleToUpdate = articleRepository.findById(articleRequestTo.getId()).orElseThrow();
        articleToUpdate.setContent(articleRequestTo.getContent());
        articleToUpdate.setTitle(articleRequestTo.getTitle());
        return mapper.toResponseDto(articleRepository.save(articleToUpdate));
    }
    @Cacheable(value = "articles", key = "#id")
    public ArticleResponseTo get(Long id) {
        return mapper.toResponseDto(articleRepository.findById(id).orElseThrow());
    }
}
