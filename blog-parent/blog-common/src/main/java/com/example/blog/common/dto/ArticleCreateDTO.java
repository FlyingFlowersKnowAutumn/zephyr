package com.example.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 500, message = "标题长度不能超过500个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String slug;

    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String excerpt;

    private String coverImage;

    private String categoryId;

    private List<String> tags;

    private String status;
}