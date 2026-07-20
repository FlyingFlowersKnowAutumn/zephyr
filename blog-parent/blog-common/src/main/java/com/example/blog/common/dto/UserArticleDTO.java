package com.example.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserArticleDTO {
    private String id;
    private String articleId;
    private String articleTitle;
    private String articleSlug;
    private String createdAt;
}
