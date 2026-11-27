package com.example.blog.user.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.*;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.service.JwtService;
import com.example.blog.user.service.UserService;
import com.example.blog.user.service.VerificationCodeService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtService jwtService;
    private final VerificationCodeService verificationCodeService;

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return Result.success(userService.login(dto, resolveClientIp(request)));
    }

    @PostMapping("/guest-login")
    public Result<LoginResponseDTO> guestLogin(@Valid @RequestBody GuestLoginDTO dto, HttpServletRequest request) {
        return Result.success(userService.guestLogin(dto, resolveClientIp(request)));
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

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO dto, HttpServletRequest request) {
        boolean success = verificationCodeService.sendCode(
                dto.getEmail(), dto.getType(), resolveClientIp(request));
        if (!success) {
            throw BusinessException.tooManyRequests("发送过于频繁或邮件发送失败，请稍后重试");
        }
        return Result.success(null);
    }

    /** 取真实客户端 IP（nginx 会透传 X-Forwarded-For） */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PutMapping("/password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.success(null);
    }

    @PutMapping("/password/self")
    public Result<Void> updatePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody UpdatePasswordDTO dto) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("未登录");
        }
        
        String token = authorization.substring(7);
        Claims claims = jwtService.parseToken(token);
        
        if (claims == null) {
            throw BusinessException.unauthorized("无效的token");
        }
        
        UUID userId = jwtService.getUserId(claims);
        userService.updatePassword(userId, dto);
        
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
