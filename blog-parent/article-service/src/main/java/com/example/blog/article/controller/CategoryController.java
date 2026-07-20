package com.example.blog.article.controller;

import com.example.blog.article.entity.Category;
import com.example.blog.article.service.CategoryService;
import com.example.blog.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable String id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @PostMapping
    public Result<Category> createCategory(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        return Result.success(categoryService.createCategory(name, description));
    }

    @PutMapping("/{id}")
    public Result<Category> updateCategory(@PathVariable String id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        return Result.success(categoryService.updateCategory(id, name, description));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return Result.success(null);
    }
}