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

    private static final String ARTICLE_ID = "969f3060-e528-4d37-a400-1ea6cb00faec";
    private static final String USER_ID = "6e2ca12d-5d59-43ee-a0b3-c7f50127fef8";
    private static final String PARENT_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @Mock private CommentRepository commentRepository;
    @Mock private SensitiveWordFilter sensitiveWordFilter;
    @Mock private ArticleStatsClient articleStatsClient;
    @InjectMocks private CommentService commentService;

    @Test
    void shouldCreateCommentWithFilteredContent() {
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId(ARTICLE_ID);
        dto.setContent("正常评论");

        when(sensitiveWordFilter.filter("正常评论")).thenReturn("正常评论");
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CommentDTO result = commentService.createComment(dto, USER_ID, "User");

        assertNotNull(result);
        assertEquals(ARTICLE_ID, result.getArticleId());
        assertEquals("正常评论", result.getContent());
        verify(sensitiveWordFilter).filter("正常评论");
        verify(articleStatsClient).syncCommentCount(ARTICLE_ID, 1);
    }

    @Test
    void shouldFilterSensitiveWordsInComment() {
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId(ARTICLE_ID);
        dto.setContent("包含敏感词");

        when(sensitiveWordFilter.filter("包含敏感词")).thenReturn("包含**");
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CommentDTO result = commentService.createComment(dto, USER_ID, "User");

        assertEquals("包含**", result.getContent());
    }

    @Test
    void shouldCreateReplyToParent() {
        Comment parent = Comment.builder()
                .id(UUID.fromString(PARENT_ID))
                .articleId(UUID.fromString(ARTICLE_ID))
                .userId(UUID.fromString(USER_ID))
                .content("parent comment")
                .build();

        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId(ARTICLE_ID);
        dto.setContent("reply");
        dto.setParentId(PARENT_ID);

        when(commentRepository.findById(UUID.fromString(PARENT_ID))).thenReturn(Optional.of(parent));
        when(sensitiveWordFilter.filter("reply")).thenReturn("reply");
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CommentDTO result = commentService.createComment(dto, USER_ID, "User");

        assertNotNull(result);
        assertEquals(PARENT_ID, result.getParentId());
    }

    @Test
    void shouldThrowWhenParentNotFound() {
        UUID missingParentId = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");

        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setArticleId(ARTICLE_ID);
        dto.setContent("reply");
        dto.setParentId(missingParentId.toString());

        when(commentRepository.findById(missingParentId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> commentService.createComment(dto, USER_ID, "User"));
    }
}
