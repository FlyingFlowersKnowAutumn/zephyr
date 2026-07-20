package com.example.blog.interaction.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.GuestbookCreateDTO;
import com.example.blog.common.dto.GuestbookDTO;
import com.example.blog.interaction.service.GuestbookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/guestbook")
@RequiredArgsConstructor
public class GuestbookController {

    private final GuestbookService guestbookService;

    @PostMapping
    public Result<GuestbookDTO> createGuestbook(
            @Valid @RequestBody GuestbookCreateDTO dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "匿名用户") String userName) {
        return Result.success(guestbookService.createGuestbook(dto, userId, userName));
    }

    @GetMapping
    public Result<Page<GuestbookDTO>> getGuestbookList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        boolean isAdmin = "blogger".equals(role) || "admin".equals(role);
        return Result.success(guestbookService.getGuestbookList(page, size, isAdmin));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteGuestbook(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        boolean isAdmin = "blogger".equals(role) || "admin".equals(role);
        guestbookService.deleteGuestbook(id, userId, isAdmin);
        return Result.success(null);
    }

    @GetMapping("/user/{userId}")
    public Result<Page<GuestbookDTO>> getGuestbookByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(guestbookService.getGuestbookByUserId(userId, page, size));
    }
}