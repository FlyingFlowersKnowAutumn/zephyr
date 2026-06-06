package com.example.blog.interaction.service;

import com.example.blog.common.dto.CommentCreateDTO;
import com.example.blog.common.dto.CommentDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.interaction.client.ArticleStatsClient;
import com.example.blog.interaction.entity.Comment;
import com.example.blog.interaction.repository.CommentRepository;
import com.example.blog.interaction.util.SensitiveWordFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private SensitiveWordFilter sensitiveWordFilter;
    @Mock private ArticleStatsClient articleStatsClient;
    @InjectMocks private CommentService commentService;

    @Test
    void shouldCreateCommentWithFilteredContent() {
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId("article-1");
        dto.setContent("正常评论");

        when(sensitiveWordFilter.filter("正常评论")).thenReturn("正常评论");
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID().toString());
            return c;
        });

        CommentDTO result = commentService.createComment(dto, "user-1", "User");

        assertNotNull(result);
        assertEquals("article-1", result.getArticleId());
        assertEquals("正常评论", result.getContent());
        verify(sensitiveWordFilter).filter("正常评论");
        verify(articleStatsClient).syncCommentCount("article-1", 1);
    }

    @Test
    void shouldFilterSensitiveWordsInComment() {
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId("article-1");
        dto.setContent("包含敏感词");

        when(sensitiveWordFilter.filter("包含敏感词")).thenReturn("包含**");
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID().toString());
            return c;
        });

        CommentDTO result = commentService.createComment(dto, "user-1", "User");

        assertEquals("包含**", result.getContent());
    }

    @Test
    void shouldCreateReplyToParent() {
        Comment parent = Comment.builder()
                .id("parent-id")
                .articleId("article-1")
                .userId("other-user")
                .content("parent comment")
                .build();

        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId("article-1");
        dto.setContent("reply");
        dto.setParentId("parent-id");

        when(commentRepository.findById("parent-id")).thenReturn(Optional.of(parent));
        when(sensitiveWordFilter.filter("reply")).thenReturn("reply");
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID().toString());
            return c;
        });

        CommentDTO result = commentService.createComment(dto, "user-1", "User");

        assertNotNull(result);
        assertEquals("parent-id", result.getParentId());
    }

    @Test
    void shouldThrowWhenParentNotFound() {
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId("article-1");
        dto.setContent("reply");
        dto.setParentId("missing-parent");

        when(commentRepository.findById("missing-parent")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> commentService.createComment(dto, "user-1", "User"));
    }
}
