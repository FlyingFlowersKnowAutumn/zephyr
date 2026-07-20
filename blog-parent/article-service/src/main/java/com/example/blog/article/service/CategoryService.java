package com.example.blog.article.service;

import com.example.blog.article.entity.Category;
import com.example.blog.article.repository.CategoryRepository;
import com.example.blog.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw BusinessException.badRequest("分类已存在");
        }

        Category category = Category.builder()
                .name(name)
                .description(description)
                .articleCount(0)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));
    }

    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));
    }

    @Transactional
    public Category updateCategory(String id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));

        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw BusinessException.badRequest("分类名称已存在");
        }

        category.setName(name);
        category.setDescription(description);

        Category updated = categoryRepository.save(category);
        log.info("Category updated: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));

        if (category.getArticleCount() > 0) {
            throw BusinessException.badRequest("分类下还有文章，无法删除");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: id={}, name={}", id, category.getName());
    }

    @Transactional
    public void incrementArticleCount(String categoryId) {
        if (categoryId != null && !categoryId.isBlank()) {
            categoryRepository.incrementArticleCount(categoryId);
        }
    }

    @Transactional
    public void decrementArticleCount(String categoryId) {
        if (categoryId != null && !categoryId.isBlank()) {
            categoryRepository.decrementArticleCount(categoryId);
        }
    }
}