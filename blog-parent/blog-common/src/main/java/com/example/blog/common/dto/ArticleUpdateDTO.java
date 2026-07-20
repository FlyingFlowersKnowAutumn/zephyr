package com.example.blog.common.dto;

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
public class ArticleUpdateDTO {
    @Size(max = 500, message = "标题长度不能超过500个字符")
    private String title;

    private String content;

    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String excerpt;

    private String coverImage;

    private String status;

    private String categoryId;

    private List<String> tags;
}