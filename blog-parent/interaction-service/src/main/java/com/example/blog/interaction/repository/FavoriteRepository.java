package com.example.blog.interaction.repository;

import com.example.blog.interaction.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    @Query("SELECT f FROM Favorite f WHERE f.userId = :userId AND f.articleId = :articleId")
    Optional<Favorite> findByUserIdAndArticleId(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.articleId = :articleId")
    long countByArticleId(@Param("articleId") UUID articleId);

    @Query("SELECT COUNT(f) > 0 FROM Favorite f WHERE f.userId = :userId AND f.articleId = :articleId")
    boolean existsByUserIdAndArticleId(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Query("SELECT f FROM Favorite f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);
}
