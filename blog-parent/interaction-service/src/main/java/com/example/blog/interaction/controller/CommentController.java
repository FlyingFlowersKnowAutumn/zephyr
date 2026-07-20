package com.example.blog.interaction.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.CommentCreateDTO;
import com.example.blog.common.dto.CommentDTO;
import com.example.blog.common.dto.CommentStatusUpdateDTO;
import com.example.blog.interaction.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<CommentDTO> createComment(
            @Valid @RequestBody CommentCreateDTO dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "匿名") String userName) {
        return Result.success(commentService.createComment(dto, userId, userName));
    }

    @GetMapping
    public Result<Page<CommentDTO>> getComments(
            @RequestParam String articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.getComments(articleId, page, size));
    }

    @GetMapping("/{id}/replies")
    public Result<List<CommentDTO>> getReplies(@PathVariable String id) {
        return Result.success(commentService.getReplies(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        boolean isAdmin = "blogger".equals(role) || "admin".equals(role);
        commentService.deleteComment(id, userId, isAdmin);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody CommentStatusUpdateDTO dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        if (!"blogger".equals(role) && !"admin".equals(role)) {
            return Result.fail(1003, "权限不足");
        }
        commentService.updateStatus(id, dto);
        return Result.success(null);
    }

    @GetMapping("/user/{userId}")
    public Result<Page<CommentDTO>> getCommentsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.getCommentsByUserId(userId, page, size));
    }

    @GetMapping("/all")
    public Result<Page<CommentDTO>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        if (!"blogger".equals(role) && !"admin".equals(role)) {
            return Result.fail(1003, "权限不足");
        }
        return Result.success(commentService.getAllComments(page, size));
    }
}
