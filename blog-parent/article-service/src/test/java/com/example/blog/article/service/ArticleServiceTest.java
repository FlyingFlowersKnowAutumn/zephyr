package com.example.blog.article.service;

import com.example.blog.article.entity.Article;
import com.example.blog.article.entity.Article.ArticleStatus;
import com.example.blog.article.repository.ArticleRepository;
import com.example.blog.common.dto.ArticleCreateDTO;
import com.example.blog.common.dto.ArticleDTO;
import com.example.blog.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock private ArticleRepository articleRepository;
    @InjectMocks private ArticleService articleService;

    @Test
    void shouldCreateArticleWithAutoSlug() {
        ArticleCreateDTO dto = new ArticleCreateDTO();
        dto.setTitle("Hello World");
        dto.setContent("This is a test article content.");
        dto.setTags(List.of("java"));

        when(articleRepository.existsBySlug(anyString())).thenReturn(false);
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> {
            Article a = inv.getArgument(0);
            a.setId("article-1");
            a.setCreatedAt(java.time.LocalDateTime.now());
            return a;
        });

        ArticleDTO result = articleService.createArticle(dto, "author-1", "Author Name");

        assertNotNull(result);
        assertEquals("Hello World", result.getTitle());
        assertEquals("article-1", result.getId());
        assertNotNull(result.getSlug());

        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(captor.capture());
        assertEquals("hello-world", captor.getValue().getSlug());
        assertEquals("This is a test article content.", captor.getValue().getContent());
    }

    @Test
    void shouldCreateArticleWithCustomSlug() {
        ArticleCreateDTO dto = new ArticleCreateDTO();
        dto.setTitle("Hello World");
        dto.setContent("Content");
        dto.setSlug("custom-slug");

        when(articleRepository.existsBySlug("custom-slug")).thenReturn(false);
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> {
            Article a = inv.getArgument(0);
            a.setId("article-1");
            return a;
        });

        ArticleDTO result = articleService.createArticle(dto, "author-1", "Author");

        assertEquals("custom-slug", result.getSlug());
    }

    @Test
    void shouldUseTsVectorSearchWhenKeywordProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Article article = Article.builder()
                .id("article-1")
                .title("Test")
                .content("Content")
                .slug("test")
                .status(ArticleStatus.PUBLISHED)
                .authorId("author-1")
                .authorName("Author")
                .build();
        Page<Article> page = new PageImpl<>(List.of(article));

        when(articleRepository.searchByTsVector("搜索词", "PUBLISHED", pageable)).thenReturn(page);

        Page<ArticleDTO> result = articleService.getArticles(ArticleStatus.PUBLISHED, null, null, "搜索词", pageable);

        assertEquals(1, result.getTotalElements());
        verify(articleRepository).searchByTsVector("搜索词", "PUBLISHED", pageable);
        verify(articleRepository, never()).findPublishedArticles(any(), any());
    }

    @Test
    void shouldUseCategoryFilterWhenCategoryIdProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Article article = Article.builder()
                .id("article-1")
                .title("Test")
                .slug("test")
                .status(ArticleStatus.PUBLISHED)
                .authorId("author-1")
                .authorName("Author")
                .build();
        Page<Article> page = new PageImpl<>(List.of(article));

        when(articleRepository.findByCategoryIdAndStatus("cat-1", ArticleStatus.PUBLISHED, pageable)).thenReturn(page);

        Page<ArticleDTO> result = articleService.getArticles(ArticleStatus.PUBLISHED, null, "cat-1", null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(articleRepository).findByCategoryIdAndStatus("cat-1", ArticleStatus.PUBLISHED, pageable);
    }

    @Test
    void shouldThrowWhenArticleNotFound() {
        when(articleRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> articleService.getArticleById("missing"));
    }
}
