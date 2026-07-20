package com.example.blog.article.service;

import com.example.blog.article.entity.Article;
import com.example.blog.article.entity.Article.ArticleStatus;
import com.example.blog.article.entity.ArticleTagMap;
import com.example.blog.article.entity.Tag;
import com.example.blog.article.repository.ArticleRepository;
import com.example.blog.article.repository.ArticleTagMapRepository;
import com.example.blog.article.service.CategoryService;
import com.example.blog.article.service.TagService;
import com.example.blog.common.dto.ArticleCreateDTO;
import com.example.blog.common.dto.ArticleDTO;
import com.example.blog.common.dto.ArticleStatsUpdateDTO;
import com.example.blog.common.dto.ArticleUpdateDTO;
import com.example.blog.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleTagMapRepository articleTagMapRepository;
    private final CategoryService categoryService;
    private final TagService tagService;

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9-]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-+");

    @Transactional
    public ArticleDTO createArticle(ArticleCreateDTO dto, String authorId, String authorName) {
        if (dto.getExcerpt() == null || dto.getExcerpt().isBlank()) {
            dto.setExcerpt(dto.getContent().length() > 500 ? dto.getContent().substring(0, 500) + "..." : dto.getContent());
        }

        String slug = ensureUniqueSlug(dto.getSlug(), dto.getTitle());

        String categoryName = null;
        if (dto.getCategoryId() != null && !dto.getCategoryId().isBlank()) {
            var category = categoryService.getCategoryById(dto.getCategoryId());
            categoryName = category.getName();
        }

        ArticleStatus status = ArticleStatus.DRAFT;
        LocalDateTime publishedAt = null;
        if (dto.getStatus() != null && "PUBLISHED".equalsIgnoreCase(dto.getStatus())) {
            status = ArticleStatus.PUBLISHED;
            publishedAt = LocalDateTime.now();
        }

        Article article = Article.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .excerpt(dto.getExcerpt())
                .coverImage(dto.getCoverImage())
                .slug(slug)
                .status(status)
                .authorId(authorId)
                .authorName(authorName)
                .categoryId(dto.getCategoryId())
                .categoryName(categoryName)
                .tags(dto.getTags() != null ? dto.getTags() : List.of())
                .publishedAt(publishedAt)
                .updatedAt(LocalDateTime.now())
                .build();

        Article saved = articleRepository.save(article);

        // 仅发布状态才增加分类/标签的计数
        if (status == ArticleStatus.PUBLISHED) {
            if (dto.getCategoryId() != null && !dto.getCategoryId().isBlank()) {
                categoryService.incrementArticleCount(dto.getCategoryId());
            }
            if (dto.getTags() != null && !dto.getTags().isEmpty()) {
                for (String tagName : dto.getTags()) {
                    Tag tag = null;
                    try {
                        tag = tagService.getTagByName(tagName);
                    } catch (BusinessException e) {
                        tag = tagService.createTag(tagName);
                    }
                    ArticleTagMap map = ArticleTagMap.builder()
                            .articleId(saved.getId())
                            .tagId(tag.getId())
                            .build();
                    articleTagMapRepository.save(map);
                    tagService.incrementArticleCount(tag.getId());
                }
            }
        } else {
            // 草稿也保存标签映射，但不增加计数
            if (dto.getTags() != null && !dto.getTags().isEmpty()) {
                for (String tagName : dto.getTags()) {
                    Tag tag = null;
                    try {
                        tag = tagService.getTagByName(tagName);
                    } catch (BusinessException e) {
                        tag = tagService.createTag(tagName);
                    }
                    ArticleTagMap map = ArticleTagMap.builder()
                            .articleId(saved.getId())
                            .tagId(tag.getId())
                            .build();
                    articleTagMapRepository.save(map);
                }
            }
        }

        log.info("Article created: id={}, slug={}, title={}, author={}", saved.getId(), saved.getSlug(), saved.getTitle(), authorName);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleById(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));
        return convertToDTO(article);
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));
        return convertToDTO(article);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDTO> getArticles(ArticleStatus status, String authorId, String categoryId, String keyword, Pageable pageable) {
        Page<Article> page;

        if (authorId != null && categoryId != null) {
            page = articleRepository.findByAuthorIdAndStatus(authorId, status, pageable);
        } else if (authorId != null) {
            page = articleRepository.findByAuthorIdAndStatus(authorId, status, pageable);
        } else if (categoryId != null) {
            page = articleRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            page = articleRepository.searchByTitle(trimmedKeyword, status, pageable);
            
            if (page.isEmpty()) {
                Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
                page = articleRepository.findByTagAndStatus(trimmedKeyword, status.name(), unsortedPageable);
                
                if (page.isEmpty()) {
                    page = articleRepository.findByCategoryNameAndStatus(trimmedKeyword, status, pageable);
                }
            }
        } else {
            page = articleRepository.findByStatus(status, pageable);
        }

        return page.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDTO> getArticlesByAuthor(String authorId, Pageable pageable) {
        return articleRepository.findByAuthorId(authorId, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDTO> getArticlesByTag(String tag, Pageable pageable) {
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return articleRepository.findByTagAndStatus(tag, ArticleStatus.PUBLISHED.name(), unsortedPageable).map(this::convertToDTO);
    }

    @Transactional
    public ArticleDTO updateArticle(String id, ArticleUpdateDTO dto, String authorId, boolean isAdmin) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));

        if (!isAdmin && !article.getAuthorId().equals(authorId)) {
            throw BusinessException.forbidden("无权修改此文章");
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            article.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            article.setContent(dto.getContent());
            if (dto.getExcerpt() == null || dto.getExcerpt().isBlank()) {
                article.setExcerpt(dto.getContent().length() > 500 ? dto.getContent().substring(0, 500) + "..." : dto.getContent());
            }
        }
        if (dto.getExcerpt() != null) {
            article.setExcerpt(dto.getExcerpt());
        }
        if (dto.getCoverImage() != null) {
            article.setCoverImage(dto.getCoverImage());
        }
        if (dto.getStatus() != null) {
            ArticleStatus newStatus = ArticleStatus.valueOf(dto.getStatus().toUpperCase());
            ArticleStatus oldStatus = article.getStatus();
            if (oldStatus != ArticleStatus.PUBLISHED && newStatus == ArticleStatus.PUBLISHED) {
                // 草稿 → 发布：增加分类和标签计数
                if (article.getCategoryId() != null && !article.getCategoryId().isBlank()) {
                    categoryService.incrementArticleCount(article.getCategoryId());
                }
                for (String tagName : article.getTags()) {
                    try {
                        Tag tag = tagService.getTagByName(tagName);
                        tagService.incrementArticleCount(tag.getId());
                    } catch (BusinessException ignored) {}
                }
            } else if (oldStatus == ArticleStatus.PUBLISHED && newStatus != ArticleStatus.PUBLISHED) {
                // 发布 → 非发布：减少分类和标签计数
                if (article.getCategoryId() != null && !article.getCategoryId().isBlank()) {
                    categoryService.decrementArticleCount(article.getCategoryId());
                }
                for (String tagName : article.getTags()) {
                    try {
                        Tag tag = tagService.getTagByName(tagName);
                        tagService.decrementArticleCount(tag.getId());
                    } catch (BusinessException ignored) {}
                }
            }
            article.setStatus(newStatus);
            if (newStatus == ArticleStatus.PUBLISHED && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }
        if (dto.getCategoryId() != null) {
            String oldCategoryId = article.getCategoryId();
            String newCategoryId = dto.getCategoryId();
            if (article.getStatus() == ArticleStatus.PUBLISHED) {
                if (oldCategoryId != null && !oldCategoryId.isBlank() && !oldCategoryId.equals(newCategoryId)) {
                    categoryService.decrementArticleCount(oldCategoryId);
                }
                if (newCategoryId != null && !newCategoryId.isBlank() && !newCategoryId.equals(oldCategoryId)) {
                    categoryService.incrementArticleCount(newCategoryId);
                }
            }
            article.setCategoryId(dto.getCategoryId());
        }
        if (dto.getTags() != null) {
            List<String> oldTags = article.getTags() != null ? article.getTags() : List.of();
            List<String> newTags = dto.getTags() != null ? dto.getTags() : List.of();
            if (article.getStatus() == ArticleStatus.PUBLISHED) {
                for (String oldTag : oldTags) {
                    if (!newTags.contains(oldTag)) {
                        try {
                            Tag tag = tagService.getTagByName(oldTag);
                            tagService.decrementArticleCount(tag.getId());
                        } catch (BusinessException ignored) {}
                    }
                }
                for (String newTag : newTags) {
                    if (!oldTags.contains(newTag)) {
                        try {
                            Tag tag = tagService.getTagByName(newTag);
                            tagService.incrementArticleCount(tag.getId());
                        } catch (BusinessException ignored) {}
                    }
                }
            }
            article.setTags(dto.getTags());
        }

        article.setUpdatedAt(LocalDateTime.now());

        Article updated = articleRepository.save(article);
        log.info("Article updated: id={}, title={}", updated.getId(), updated.getTitle());
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteArticle(String id, String authorId, boolean isAdmin) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));

        if (!isAdmin && !article.getAuthorId().equals(authorId)) {
            throw BusinessException.forbidden("无权删除此文章");
        }

        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            if (article.getCategoryId() != null && !article.getCategoryId().isBlank()) {
                categoryService.decrementArticleCount(article.getCategoryId());
            }
            for (String tagName : article.getTags()) {
                try {
                    Tag tag = tagService.getTagByName(tagName);
                    tagService.decrementArticleCount(tag.getId());
                } catch (BusinessException ignored) {}
            }
        }

        articleRepository.delete(article);
        log.info("Article deleted: id={}, title={}", id, article.getTitle());
    }

    @Transactional
    public void incrementViewCount(String id) {
        articleRepository.incrementViewCount(id);
    }

    @Transactional
    public void incrementLikeCount(String id) {
        articleRepository.incrementLikeCount(id);
    }

    @Transactional
    public void updateStats(String id, ArticleStatsUpdateDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));

        if (dto.getCommentCountDelta() != null && dto.getCommentCountDelta() != 0) {
            int newCount = Math.max(0, article.getCommentCount() + dto.getCommentCountDelta());
            article.setCommentCount(newCount);
        }
        if (dto.getLikeCountDelta() != null && dto.getLikeCountDelta() != 0) {
            int newCount = Math.max(0, article.getLikeCount() + dto.getLikeCountDelta());
            article.setLikeCount(newCount);
        }

        articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return articleRepository.findAllCategoryNames();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllTagsWithCount() {
        List<Object[]> results = articleRepository.findTagsWithCount(ArticleStatus.PUBLISHED);
        return results.stream()
                .map(row -> {
                    Map<String, Object> tagInfo = new HashMap<>();
                    tagInfo.put("name", row[0]);
                    tagInfo.put("count", row[1]);
                    return tagInfo;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return articleRepository.findAllTags();
    }

    @Transactional(readOnly = true)
    public LocalDateTime getLatestArticleTime() {
        return articleRepository.findLatestArticleTime(ArticleStatus.PUBLISHED);
    }

    private String ensureUniqueSlug(String providedSlug, String title) {
        String base = (providedSlug != null && !providedSlug.isBlank())
                ? generateSlug(providedSlug)
                : generateSlug(title);
        if (base == null || base.isBlank()) {
            base = "post-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String slug = base;
        int suffix = 2;
        while (articleRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix;
            suffix++;
        }
        return slug;
    }

    private String generateSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String ascii = normalized.replaceAll("[^\\x00-\\x7F]", "");
        if (ascii.isBlank()) {
            return "post-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String slug = ascii.toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-|-$", "");
        if (slug.length() > 200) {
            slug = slug.substring(0, 200);
        }
        if (slug.isBlank()) {
            slug = "post-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return slug;
    }

    private ArticleDTO convertToDTO(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .title(article.getTitle())
                .content(article.getContent())
                .excerpt(article.getExcerpt())
                .coverImage(article.getCoverImage())
                .status(article.getStatus().name())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .authorId(article.getAuthorId())
                .authorName(article.getAuthorName())
                .categoryId(article.getCategoryId())
                .categoryName(article.getCategoryName())
                .tags(article.getTags())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .build();
    }
}