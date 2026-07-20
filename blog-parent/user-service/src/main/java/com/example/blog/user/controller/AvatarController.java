package com.example.blog.user.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.UserProfileDTO;
import com.example.blog.user.service.FileUploadService;
import com.example.blog.user.service.JwtService;
import com.example.blog.user.service.UserProfileService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AvatarController {

    private final FileUploadService fileUploadService;
    private final UserProfileService userProfileService;
    private final JwtService jwtService;

    @PostMapping("/{userId}/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorization) throws IOException {
        
        UUID callerId = extractUserId(authorization);
        if (!callerId.equals(userId)) {
            return Result.fail(403, "无权操作");
        }
        
        String oldAvatarUrl = userProfileService.getProfile(userId).getAvatarUrl();

        String avatarUrl = fileUploadService.uploadAvatar(file);

        userProfileService.updateProfile(userId, com.example.blog.common.dto.UpdateUserProfileDTO.builder()
                .avatarUrl(avatarUrl)
                .build());

        fileUploadService.deleteOldAvatar(oldAvatarUrl);
        
        Map<String, String> result = new HashMap<>();
        result.put("avatarUrl", avatarUrl);
        return Result.success(result);
    }

    private UUID extractUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        String token = authorization.substring(7);
        Claims claims = jwtService.parseToken(token);
        if (claims == null) {
            throw new RuntimeException("无效的token");
        }
        return jwtService.getUserId(claims);
    }
}
