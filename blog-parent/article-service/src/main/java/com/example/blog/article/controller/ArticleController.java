package com.example.blog.article.controller;

import com.example.blog.article.entity.Article.ArticleStatus;
import com.example.blog.article.service.ArticleService;
import com.example.blog.common.Result;
import com.example.blog.common.dto.ArticleCreateDTO;
import com.example.blog.common.dto.ArticleDTO;
import com.example.blog.common.dto.ArticleStatsUpdateDTO;
import com.example.blog.common.dto.ArticleUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public Result<ArticleDTO> createArticle(
            @Valid @RequestBody ArticleCreateDTO dto,
            @RequestHeader("X-User-Id") String authorId,
            @RequestHeader("X-User-Name") String authorName,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        if (!"blogger".equals(role) && !"admin".equals(role)) {
            return Result.fail(403, "仅博主可发布文章");
        }
        return Result.success(articleService.createArticle(dto, authorId, authorName));
    }

    @GetMapping("/{id}")
    public Result<ArticleDTO> getArticleById(@PathVariable String id) {
        articleService.incrementViewCount(id);
        return Result.success(articleService.getArticleById(id));
    }

    @GetMapping("/slug/{slug}")
    public Result<ArticleDTO> getArticleBySlug(@PathVariable String slug) {
        ArticleDTO article = articleService.getArticleBySlug(slug);
        articleService.incrementViewCount(article.getId());
        return Result.success(article);
    }

    @GetMapping
    public Result<Page<ArticleDTO>> getArticles(
            @RequestParam(defaultValue = "PUBLISHED") String status,
            @RequestParam(required = false) String authorId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        ArticleStatus articleStatus = ArticleStatus.valueOf(status.toUpperCase());
        return Result.success(articleService.getArticles(articleStatus, authorId, categoryId, keyword, pageable));
    }

    @GetMapping("/author/{authorId}")
    public Result<Page<ArticleDTO>> getArticlesByAuthor(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(articleService.getArticlesByAuthor(authorId, pageable));
    }

    @GetMapping("/tag/{tag}")
    public Result<Page<ArticleDTO>> getArticlesByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return Result.success(articleService.getArticlesByTag(tag, pageable));
    }

    @PutMapping("/{id}")
    public Result<ArticleDTO> updateArticle(
            @PathVariable String id,
            @Valid @RequestBody ArticleUpdateDTO dto,
            @RequestHeader("X-User-Id") String authorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        boolean isAdmin = "blogger".equals(role) || "admin".equals(role);
        return Result.success(articleService.updateArticle(id, dto, authorId, isAdmin));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String authorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        boolean isAdmin = "blogger".equals(role) || "admin".equals(role);
        articleService.deleteArticle(id, authorId, isAdmin);
        return Result.success(null);
    }

    @PostMapping("/{id}/stats")
    public Result<Void> updateStats(
            @PathVariable String id,
            @RequestBody ArticleStatsUpdateDTO dto) {
        articleService.updateStats(id, dto);
        return Result.success(null);
    }

    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        return Result.success(articleService.getAllCategories());
    }

    @GetMapping("/tags")
    public Result<List<String>> getAllTags() {
        return Result.success(articleService.getAllTags());
    }

    @GetMapping("/tags/count")
    public Result<List<Map<String, Object>>> getTagsWithCount() {
        return Result.success(articleService.getAllTagsWithCount());
    }

    @GetMapping("/latest-time")
    public Result<Map<String, String>> getLatestArticleTime() {
        LocalDateTime time = articleService.getLatestArticleTime();
        Map<String, String> result = Map.of(
                "latestTime", time != null ? time.atZone(java.time.ZoneId.of("Asia/Shanghai")).toOffsetDateTime().toString() : null
        );
        return Result.success(result);
    }
}