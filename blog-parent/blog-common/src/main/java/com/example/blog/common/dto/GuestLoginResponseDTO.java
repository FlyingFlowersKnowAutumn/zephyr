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
public class GuestLoginResponseDTO {
    private String token;
    private String userId;
    private String displayName;
    private String role;
    private LocalDateTime expiresAt;
}
