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
public class UserDTO {
    private String id;
    private String username;
    private String displayName;
    private String bio;
    private String email;
    private String avatarUrl;
    private String role;
    private LocalDateTime createdAt;
}
