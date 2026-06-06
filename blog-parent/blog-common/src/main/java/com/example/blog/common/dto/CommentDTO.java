package com.example.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private String id;
    private String articleId;
    private String userId;
    private String userName;
    private String parentId;
    private String content;
    private String status;
    private List<CommentDTO> replies;
    private LocalDateTime createdAt;
}
