package com.example.blog.interaction.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.FavoriteResponseDTO;
import com.example.blog.common.dto.UserArticleDTO;
import com.example.blog.interaction.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/api/v1/articles/{id}/favorite")
    public Result<FavoriteResponseDTO> toggleFavorite(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return Result.success(favoriteService.toggleFavorite(id, userId));
    }

    @GetMapping("/api/v1/articles/{id}/favorite")
    public Result<FavoriteResponseDTO> getFavoriteStatus(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return Result.success(favoriteService.getFavoriteStatus(id, userId));
    }

    @GetMapping("/api/v1/favorites/user/{userId}")
    public Result<java.util.List<UserArticleDTO>> getUserFavorites(@PathVariable String userId) {
        return Result.success(favoriteService.getUserFavorites(userId));
    }

    @GetMapping("/api/v1/favorites/user/{userId}/count")
    public Result<Long> countUserFavorites(@PathVariable String userId) {
        return Result.success(favoriteService.countUserFavorites(userId));
    }
}
