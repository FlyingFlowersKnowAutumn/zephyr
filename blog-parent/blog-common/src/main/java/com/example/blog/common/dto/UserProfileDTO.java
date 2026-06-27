package com.example.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private String userId;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String gender;
    private String website;
    private String github;
    private String twitter;
    private String about;
}
