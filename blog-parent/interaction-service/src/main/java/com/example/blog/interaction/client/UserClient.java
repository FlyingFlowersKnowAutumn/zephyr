package com.example.blog.interaction.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class UserClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String USER_SERVICE_URL = "http://user-service:8002/api/v1/users";

    public UserProfileResponse getUserProfile(String userId) {
        try {
            return restTemplate.getForObject(USER_SERVICE_URL + "/" + userId + "/profile", UserProfileResponse.class);
        } catch (Exception e) {
            log.warn("Failed to get user profile for userId {}: {}", userId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class UserProfileResponse {
        private Integer code;
        private String message;
        private UserProfileDTO data;
        private String traceId;

        public boolean isSuccess() {
            return code != null && code == 0;
        }
    }

    @Data
    public static class UserProfileDTO {
        private String userId;
        private String displayName;
        private String avatarUrl;
        private String bio;
        private String gender;
        private String website;
        private String github;
        private String twitter;
        private String about;
    }
}
