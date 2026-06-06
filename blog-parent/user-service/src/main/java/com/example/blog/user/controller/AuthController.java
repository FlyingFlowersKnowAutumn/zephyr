package com.example.blog.user.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.*;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.service.GuestAuthService;
import com.example.blog.user.service.JwtService;
import com.example.blog.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final GuestAuthService guestAuthService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/guest-login")
    public Result<GuestLoginResponseDTO> guestLogin() {
        return Result.success(guestAuthService.guestLogin());
    }

    @DeleteMapping("/guest")
    public Result<Void> deleteGuest(@RequestHeader("X-User-Id") String userId) {
        guestAuthService.deleteGuest(UUID.fromString(userId));
        return Result.success(null);
    }

    @PostMapping("/refresh")
    public Result<LoginResponseDTO> refresh(@RequestBody RefreshTokenDTO dto) {
        throw BusinessException.notFound("暂未实现");
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody LogoutDTO dto) {
        userService.logout(dto.getRefreshToken());
        return Result.success(null);
    }

    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("未登录");
        }
        
        String token = authorization.substring(7);
        Claims claims = jwtService.parseToken(token);
        
        if (claims == null) {
            throw BusinessException.unauthorized("无效的token");
        }
        
        UUID userId = jwtService.getUserId(claims);
        return Result.success(userService.getCurrentUser(userId));
    }
}
