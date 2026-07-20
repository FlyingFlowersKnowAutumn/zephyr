package com.example.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestbookCreateDTO {
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容不能超过1000字")
    private String content;

    private String avatarUrl;
}