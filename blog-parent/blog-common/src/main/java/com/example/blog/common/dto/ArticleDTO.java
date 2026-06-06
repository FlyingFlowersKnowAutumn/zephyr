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
public class ArticleDTO {
    private String id;
    private String slug;
    private String title;
    private String content;
    private String excerpt;
    private String coverImage;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String authorId;
    private String authorName;
    private String categoryId;
    private String categoryName;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}