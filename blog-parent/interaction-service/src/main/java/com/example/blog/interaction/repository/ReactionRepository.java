package com.example.blog.interaction.repository;

import com.example.blog.interaction.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, String> {

    Optional<Reaction> findByUserIdAndArticleId(String userId, String articleId);

    long countByArticleId(String articleId);

    boolean existsByUserIdAndArticleId(String userId, String articleId);
}
