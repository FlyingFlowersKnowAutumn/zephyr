package com.example.blog.interaction.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.ReactionResponseDTO;
import com.example.blog.common.dto.UserArticleDTO;
import com.example.blog.interaction.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/api/v1/articles/{id}/reaction")
    public Result<ReactionResponseDTO> toggleReaction(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return Result.success(reactionService.toggleReaction(id, userId));
    }

    @GetMapping("/api/v1/articles/{id}/reaction")
    public Result<ReactionResponseDTO> getReactionStatus(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            long count = reactionService.getReactionStatus(id, null).getLikeCount();
            return Result.success(ReactionResponseDTO.builder().liked(false).likeCount(count).build());
        }
        return Result.success(reactionService.getReactionStatus(id, userId));
    }

    @GetMapping("/api/v1/reactions/user/{userId}")
    public Result<java.util.List<UserArticleDTO>> getUserReactions(@PathVariable String userId) {
        return Result.success(reactionService.getUserReactions(userId));
    }

    @GetMapping("/api/v1/reactions/user/{userId}/count")
    public Result<Long> countUserReactions(@PathVariable String userId) {
        return Result.success(reactionService.countUserReactions(userId));
    }
}
