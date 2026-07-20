package com.example.blog.interaction.service;

import com.example.blog.common.dto.CommentCreateDTO;
import com.example.blog.common.dto.CommentDTO;
import com.example.blog.common.dto.CommentStatusUpdateDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.interaction.client.ArticleClient;
import com.example.blog.interaction.client.ArticleStatsClient;
import com.example.blog.interaction.client.UserClient;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final ArticleStatsClient articleStatsClient;
    private final ArticleClient articleClient;
    private final UserClient userClient;

    @Transactional
    public CommentDTO createComment(CommentCreateDTO dto, String userId, String userName) {
        UUID articleUuid = UUID.fromString(dto.getArticleId());
        UUID userUuid = UUID.fromString(userId);
        UUID parentUuid = dto.getParentId() != null ? UUID.fromString(dto.getParentId()) : null;
        UUID replyToUserId = null;

        if (parentUuid != null) {
            Comment parent = commentRepository.findById(parentUuid)
                    .orElseThrow(() -> BusinessException.notFound("父评论不存在"));
            replyToUserId = parent.getUserId();
        }

        String filteredContent = sensitiveWordFilter.filter(dto.getContent());

        Comment comment = Comment.builder()
                .articleId(articleUuid)
                .userId(userUuid)
                .parentId(parentUuid)
                .content(filteredContent)
                .images(dto.getImages() != null && !dto.getImages().isEmpty()
                        ? String.join(",", dto.getImages())
                        : null)
                .build();

        Comment saved = commentRepository.save(comment);
        articleStatsClient.syncCommentCount(dto.getArticleId(), 1);

        log.info("Comment created: id={}, articleId={}, userId={}", saved.getId(), saved.getArticleId(), userId);
        return convertToDTO(saved, replyToUserId);
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getComments(String articleId, int page, int size) {
        UUID articleUuid = UUID.fromString(articleId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> topLevelPage = commentRepository.findTopLevelByArticleId(articleUuid, pageable);

        return topLevelPage.map(comment -> {
            CommentDTO dto = convertToDTO(comment);
            List<Comment> replies = commentRepository.findRepliesByParentId(comment.getId());
            dto.setReplies(replies.stream()
                    .map(r -> {
                        CommentDTO replyDto = convertToDTO(r, comment.getUserId());
                        replyDto.setReplies(getNestedReplies(r.getId()));
                        return replyDto;
                    })
                    .collect(Collectors.toList()));
            return dto;
        });
    }

    private List<CommentDTO> getNestedReplies(UUID parentId) {
        Comment parent = commentRepository.findById(parentId).orElse(null);
        UUID parentUserId = parent != null ? parent.getUserId() : null;
        
        List<Comment> replies = commentRepository.findRepliesByParentId(parentId);
        return replies.stream()
                .map(r -> {
                    CommentDTO replyDto = convertToDTO(r, parentUserId);
                    List<CommentDTO> nested = getNestedReplies(r.getId());
                    if (!nested.isEmpty()) {
                        replyDto.setReplies(nested);
                    }
                    return replyDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getReplies(String commentId) {
        UUID parentUuid = UUID.fromString(commentId);
        return commentRepository.findRepliesByParentId(parentUuid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(String id, String userId, boolean isAdmin) {
        UUID uuid = UUID.fromString(id);
        Comment comment = commentRepository.findById(uuid)
                .orElseThrow(() -> BusinessException.notFound("评论不存在"));

        if (!isAdmin && !comment.getUserId().toString().equals(userId)) {
            throw BusinessException.forbidden("无权删除此评论");
        }

        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
        articleStatsClient.syncCommentCount(comment.getArticleId().toString(), -1);

        log.info("Comment deleted: id={}, articleId={}", id, comment.getArticleId());
    }

    @Transactional
    public void updateStatus(String id, CommentStatusUpdateDTO dto) {
        UUID uuid = UUID.fromString(id);
        Comment comment = commentRepository.findById(uuid)
                .orElseThrow(() -> BusinessException.notFound("评论不存在"));
        comment.setStatus(dto.getStatus());
        commentRepository.save(comment);
        log.info("Comment status updated: id={}, status={}", id, dto.getStatus());
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsByUserId(String userId, int page, int size) {
        UUID userUuid = UUID.fromString(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByUserId(userUuid, pageable);

        return commentPage.map(comment -> {
            CommentDTO dto = convertToDTOWithArticle(comment);
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getAllComments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findAllNotDeleted(pageable);

        return commentPage.map(comment -> {
            CommentDTO dto = convertToDTOWithArticle(comment);
            return dto;
        });
    }

    private CommentDTO convertToDTOWithArticle(Comment comment) {
        CommentDTO dto = convertToDTO(comment);
        try {
            ArticleClient.ArticleInfo articleInfo = articleClient.getArticleInfo(comment.getArticleId().toString());
            if (articleInfo != null) {
                dto.setArticleTitle(articleInfo.getTitle());
                dto.setArticleSlug(articleInfo.getSlug());
            }
        } catch (Exception e) {
            log.warn("Failed to get article info for comment {}: {}", comment.getId(), e.getMessage());
        }
        return dto;
    }

    private CommentDTO convertToDTO(Comment comment, UUID replyToUserId) {
        List<String> imageList = comment.getImages() != null && !comment.getImages().isEmpty()
                ? List.of(comment.getImages().split(","))
                : Collections.emptyList();

        String avatarUrl = null;
        String displayName = null;
        String replyToUserName = null;

        UserClient.UserProfileResponse profileResponse = userClient.getUserProfile(comment.getUserId().toString());
        if (profileResponse != null && profileResponse.isSuccess() && profileResponse.getData() != null) {
            if (profileResponse.getData().getDisplayName() != null && !profileResponse.getData().getDisplayName().isEmpty()) {
                displayName = profileResponse.getData().getDisplayName();
            }
            avatarUrl = profileResponse.getData().getAvatarUrl();
        }

        if (replyToUserId != null) {
            UserClient.UserProfileResponse replyToProfile = userClient.getUserProfile(replyToUserId.toString());
            if (replyToProfile != null && replyToProfile.isSuccess() && replyToProfile.getData() != null) {
                if (replyToProfile.getData().getDisplayName() != null && !replyToProfile.getData().getDisplayName().isEmpty()) {
                    replyToUserName = replyToProfile.getData().getDisplayName();
                }
            }
        }

        if (displayName == null || displayName.isEmpty()) {
            displayName = "匿名";
        }

        return CommentDTO.builder()
                .id(comment.getId().toString())
                .articleId(comment.getArticleId().toString())
                .userId(comment.getUserId().toString())
                .userName(displayName)
                .avatarUrl(avatarUrl)
                .parentId(comment.getParentId() != null ? comment.getParentId().toString() : null)
                .replyToUserName(replyToUserName)
                .content(comment.getContent())
                .status(comment.getStatus())
                .images(imageList)
                .replies(Collections.emptyList())
                .createdAt(comment.getCreatedAt() != null 
                        ? comment.getCreatedAt().atZone(ZoneId.of("Asia/Shanghai"))
                        : ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
                .build();
    }

    private CommentDTO convertToDTO(Comment comment) {
        return convertToDTO(comment, null);
    }
}
