package com.example.blog.interaction.repository;

import com.example.blog.interaction.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    @Query("SELECT r FROM Reaction r WHERE r.userId = :userId AND r.articleId = :articleId")
    Optional<Reaction> findByUserIdAndArticleId(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.articleId = :articleId")
    long countByArticleId(@Param("articleId") UUID articleId);

    @Query("SELECT COUNT(r) > 0 FROM Reaction r WHERE r.userId = :userId AND r.articleId = :articleId")
    boolean existsByUserIdAndArticleId(@Param("userId") UUID userId, @Param("articleId") UUID articleId);
}
