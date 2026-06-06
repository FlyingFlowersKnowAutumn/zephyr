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
public class QuestionDTO {
    private String id;
    private String userId;
    private String userName;
    private String title;
    private String content;
    private String status;
    private boolean isPublic;
    private List<AnswerDTO> answers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
