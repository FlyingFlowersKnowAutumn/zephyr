package com.example.blog.article.repository;

import com.example.blog.article.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount + 1 WHERE c.id = :id")
    void incrementArticleCount(@Param("id") String id);

    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount - 1 WHERE c.id = :id AND c.articleCount > 0")
    void decrementArticleCount(@Param("id") String id);
}