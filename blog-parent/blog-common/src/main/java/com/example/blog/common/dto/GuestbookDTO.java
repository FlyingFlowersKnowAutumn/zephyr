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
public class GuestbookDTO {
    private String id;
    private String userId;
    private String userName;
    private String avatarUrl;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}