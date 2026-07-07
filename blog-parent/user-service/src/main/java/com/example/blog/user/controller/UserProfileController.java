package com.example.blog.user.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.AnnouncementDTO;
import com.example.blog.common.dto.UpdateAnnouncementDTO;
import com.example.blog.common.dto.UpdateUserProfileDTO;
import com.example.blog.common.dto.UserProfileDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.service.JwtService;
import com.example.blog.user.service.UserProfileService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final JwtService jwtService;

    @GetMapping("/{userId}/profile")
    public Result<UserProfileDTO> getProfile(@PathVariable UUID userId) {
        return Result.success(userProfileService.getProfile(userId));
    }

    @GetMapping("/{userId}/profile/info")
    public Result<UserProfileDTO> getProfileInfo(@PathVariable UUID userId) {
        return Result.success(userProfileService.getProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    public Result<UserProfileDTO> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserProfileDTO dto,
            @RequestHeader("Authorization") String authorization) {
        UUID callerId = extractUserId(authorization);
        if (!callerId.equals(userId)) {
            throw BusinessException.forbidden("无权操作");
        }
        return Result.success(userProfileService.updateProfile(userId, dto));
    }

    private UUID extractUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("未登录");
        }
        String token = authorization.substring(7);
        Claims claims = jwtService.parseToken(token);
        if (claims == null) {
            throw BusinessException.unauthorized("无效的token");
        }
        return jwtService.getUserId(claims);
    }

    @GetMapping("/{userId}/announcement")
    public Result<AnnouncementDTO> getAnnouncement(@PathVariable UUID userId) {
        return Result.success(userProfileService.getAnnouncement(userId));
    }

    @PutMapping("/{userId}/announcement")
    public Result<AnnouncementDTO> updateAnnouncement(
            @PathVariable UUID userId,
            @RequestBody UpdateAnnouncementDTO dto,
            @RequestHeader("Authorization") String authorization) {
        UUID callerId = extractUserId(authorization);
        if (!callerId.equals(userId)) {
            throw BusinessException.forbidden("无权操作");
        }
        return Result.success(userProfileService.updateAnnouncement(userId, dto));
    }
}
