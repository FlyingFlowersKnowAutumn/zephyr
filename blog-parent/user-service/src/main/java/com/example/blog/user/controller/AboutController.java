package com.example.blog.user.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.AboutDTO;
import com.example.blog.common.dto.UpdateAboutDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.service.AboutService;
import com.example.blog.user.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;
    private final JwtService jwtService;

    @GetMapping("/about")
    public Result<AboutDTO> getAbout() {
        return Result.success(aboutService.getAbout());
    }

    @PutMapping("/about")
    public Result<AboutDTO> updateAbout(
            @Valid @RequestBody UpdateAboutDTO dto,
            @RequestHeader("Authorization") String authorization) {
        UUID userId = extractUserId(authorization);
        return Result.success(aboutService.saveAbout(userId, dto));
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
}
