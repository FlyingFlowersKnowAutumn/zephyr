package com.example.blog.user.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.UserDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.User;
import com.example.blog.user.repository.UserRepository;
import com.example.blog.user.service.JwtService;
import com.example.blog.user.service.RbacService;
import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RbacService rbacService;

    @PutMapping("/{userId}/bio")
    public Result<UserDTO> updateUserBio(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String userId,
            @RequestBody UpdateBioRequest request) {
        
        UUID adminId = validateAdmin(authorization);
        
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        user.setBio(request.getBio());
        User saved = userRepository.save(user);
        
        log.info("Admin {} updated bio for user {}", adminId, userId);
        
        return Result.success(convertToDTO(saved));
    }

    @PostMapping("/{userId}/role/{roleName}")
    public Result<Void> assignRole(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String userId,
            @PathVariable String roleName) {
        
        validateAdmin(authorization);
        
        rbacService.assignRoleToUser(UUID.fromString(userId), roleName);
        return Result.success(null);
    }

    @DeleteMapping("/{userId}/role/{roleName}")
    public Result<Void> removeRole(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String userId,
            @PathVariable String roleName) {
        
        validateAdmin(authorization);
        
        rbacService.removeRoleFromUser(UUID.fromString(userId), roleName);
        return Result.success(null);
    }

    private UUID validateAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("未登录");
        }
        
        String token = authorization.substring(7);
        Claims claims = jwtService.parseToken(token);
        
        if (claims == null) {
            throw BusinessException.unauthorized("无效的token");
        }
        
        UUID userId = jwtService.getUserId(claims);
        
        if (!rbacService.isAdmin(userId)) {
            throw BusinessException.forbidden("无权限操作");
        }
        
        return userId;
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static class UpdateBioRequest {
        @NotBlank(message = "签名不能为空")
        private String bio;

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }
    }
}