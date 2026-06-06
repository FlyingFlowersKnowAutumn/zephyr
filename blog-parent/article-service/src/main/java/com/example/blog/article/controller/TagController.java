package com.example.blog.article.controller;

import com.example.blog.article.service.ArticleService;
import com.example.blog.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final ArticleService articleService;

    @GetMapping
    public Result<List<String>> getAllTags() {
        return Result.success(articleService.getAllTags());
    }

    @GetMapping("/count")
    public Result<List<Map<String, Object>>> getTagsWithCount() {
        return Result.success(articleService.getAllTagsWithCount());
    }
}