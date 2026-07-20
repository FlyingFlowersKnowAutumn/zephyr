package com.example.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentStatusUpdateDTO {
    @NotBlank(message = "状态不能为空")
    private String status;
}
