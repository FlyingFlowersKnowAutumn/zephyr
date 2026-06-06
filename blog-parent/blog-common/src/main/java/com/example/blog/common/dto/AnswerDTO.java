package com.example.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerDTO {
    private String id;
    private String questionId;
    private String userId;
    private String userName;
    private String content;
    private boolean isAccepted;
    private String status;
    private LocalDateTime createdAt;
}
