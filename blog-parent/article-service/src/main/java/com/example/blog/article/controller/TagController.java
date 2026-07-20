package com.example.blog.article.controller;

import com.example.blog.article.entity.Tag;
import com.example.blog.article.service.TagService;
import com.example.blog.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<Tag>> getAllTags() {
        return Result.success(tagService.getAllTags());
    }

    @GetMapping("/hot")
    public Result<List<Tag>> getHotTags() {
        return Result.success(tagService.getHotTags());
    }

    @GetMapping("/{id}")
    public Result<Tag> getTagById(@PathVariable String id) {
        return Result.success(tagService.getTagById(id));
    }

    @PostMapping
    public Result<Tag> createTag(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        return Result.success(tagService.createTag(name));
    }

    @PutMapping("/{id}")
    public Result<Tag> updateTag(@PathVariable String id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        return Result.success(tagService.updateTag(id, name));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/visit")
    public Result<Void> incrementVisit(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        tagService.incrementVisitCount(id, userId);
        return Result.success(null);
    }
}