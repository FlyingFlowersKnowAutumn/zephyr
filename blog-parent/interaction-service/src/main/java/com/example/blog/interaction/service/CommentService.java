package com.example.blog.interaction.service;

import com.example.blog.common.dto.CommentCreateDTO;
import com.example.blog.common.dto.CommentDTO;
import com.example.blog.common.dto.CommentStatusUpdateDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.interaction.client.ArticleStatsClient;
import com.example.blog.interaction.entity.Comment;
import com.example.blog.interaction.repository.CommentRepository;
import com.example.blog.interaction.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final ArticleStatsClient articleStatsClient;

    @Transactional
    public CommentDTO createComment(CommentCreateDTO dto, String userId, String userName) {
        if (dto.getParentId() != null) {
            Comment parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> BusinessException.notFound("父评论不存在"));
            if (!parent.isTopLevel()) {
                throw BusinessException.badRequest("仅支持一级回复");
            }
        }

        String filteredContent = sensitiveWordFilter.filter(dto.getContent());

        Comment comment = Comment.builder()
                .articleId(dto.getArticleId())
                .userId(userId)
                .parentId(dto.getParentId())
                .content(filteredContent)
                .build();

        Comment saved = commentRepository.save(comment);
        articleStatsClient.syncCommentCount(dto.getArticleId(), 1);

        log.info("Comment created: id={}, articleId={}, userId={}", saved.getId(), saved.getArticleId(), userId);
        return convertToDTO(saved, userName);
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getComments(String articleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> topLevelPage = commentRepository.findTopLevelByArticleId(articleId, pageable);

        return topLevelPage.map(comment -> {
            CommentDTO dto = convertToDTO(comment, null);
            List<Comment> replies = commentRepository.findRepliesByParentId(comment.getId());
            dto.setReplies(replies.stream()
                    .map(r -> convertToDTO(r, null))
                    .collect(Collectors.toList()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getReplies(String commentId) {
        return commentRepository.findRepliesByParentId(commentId).stream()
                .map(r -> convertToDTO(r, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(String id, String userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("评论不存在"));

        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此评论");
        }

        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
        articleStatsClient.syncCommentCount(comment.getArticleId(), -1);

        log.info("Comment deleted: id={}, articleId={}", id, comment.getArticleId());
    }

    @Transactional
    public void updateStatus(String id, CommentStatusUpdateDTO dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("评论不存在"));
        comment.setStatus(dto.getStatus());
        commentRepository.save(comment);
        log.info("Comment status updated: id={}, status={}", id, dto.getStatus());
    }

    private CommentDTO convertToDTO(Comment comment, String userName) {
        return CommentDTO.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(comment.getUserId())
                .userName(userName)
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .status(comment.getStatus())
                .replies(Collections.emptyList())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
