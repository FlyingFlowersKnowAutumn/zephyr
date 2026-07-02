package com.example.blog.interaction.repository;

import com.example.blog.interaction.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, String> {

    Optional<Favorite> findByUserIdAndArticleId(String userId, String articleId);

    long countByArticleId(String articleId);

    boolean existsByUserIdAndArticleId(String userId, String articleId);
}
