package com.example.blog.interaction.service;

import com.example.blog.common.dto.FavoriteResponseDTO;
import com.example.blog.interaction.entity.Favorite;
import com.example.blog.interaction.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Transactional
    public FavoriteResponseDTO toggleFavorite(String articleId, String userId) {
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndArticleId(userId, articleId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            long count = favoriteRepository.countByArticleId(articleId);
            log.info("Favorite removed: articleId={}, userId={}", articleId, userId);
            return FavoriteResponseDTO.builder().favorited(false).favoriteCount(count).build();
        } else {
            Favorite favorite = Favorite.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .build();
            favoriteRepository.save(favorite);
            long count = favoriteRepository.countByArticleId(articleId);
            log.info("Favorite added: articleId={}, userId={}", articleId, userId);
            return FavoriteResponseDTO.builder().favorited(true).favoriteCount(count).build();
        }
    }

    @Transactional(readOnly = true)
    public FavoriteResponseDTO getFavoriteStatus(String articleId, String userId) {
        boolean favorited = userId != null && favoriteRepository.existsByUserIdAndArticleId(userId, articleId);
        long favoriteCount = favoriteRepository.countByArticleId(articleId);
        return FavoriteResponseDTO.builder().favorited(favorited).favoriteCount(favoriteCount).build();
    }
}
