package com.example.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private String id;
    private String articleId;
    private String articleTitle;
    private String articleSlug;
    private String userId;
    private String userName;
    private String avatarUrl;
    private String parentId;
    private String replyToUserName;
    private String content;
    private String status;
    private List<CommentDTO> replies;
    private List<String> images;
    private ZonedDateTime createdAt;
}
