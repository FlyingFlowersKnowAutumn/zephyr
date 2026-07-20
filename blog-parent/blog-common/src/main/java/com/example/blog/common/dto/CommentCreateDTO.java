package com.example.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CommentCreateDTO {
    @NotBlank(message = "文章ID不能为空")
    private String articleId;

    private String parentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private List<String> images;
}
