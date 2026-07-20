package com.example.blog.interaction.service;

import com.example.blog.common.dto.FavoriteResponseDTO;
import com.example.blog.common.dto.UserArticleDTO;
import com.example.blog.interaction.client.ArticleClient;
import com.example.blog.interaction.entity.Favorite;
import com.example.blog.interaction.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ArticleClient articleClient;

    @Transactional
    public FavoriteResponseDTO toggleFavorite(String articleId, String userId) {
        UUID articleUuid = UUID.fromString(articleId);
        UUID userUuid = UUID.fromString(userId);
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndArticleId(userUuid, articleUuid);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            long count = favoriteRepository.countByArticleId(articleUuid);
            log.info("Favorite removed: articleId={}, userId={}", articleId, userId);
            return FavoriteResponseDTO.builder().favorited(false).favoriteCount(count).build();
        } else {
            Favorite favorite = Favorite.builder()
                    .articleId(articleUuid)
                    .userId(userUuid)
                    .build();
            favoriteRepository.save(favorite);
            long count = favoriteRepository.countByArticleId(articleUuid);
            log.info("Favorite added: articleId={}, userId={}", articleId, userId);
            return FavoriteResponseDTO.builder().favorited(true).favoriteCount(count).build();
        }
    }

    @Transactional(readOnly = true)
    public FavoriteResponseDTO getFavoriteStatus(String articleId, String userId) {
        UUID articleUuid = UUID.fromString(articleId);
        boolean favorited;
        long favoriteCount;
        if (userId != null) {
            UUID userUuid = UUID.fromString(userId);
            favorited = favoriteRepository.existsByUserIdAndArticleId(userUuid, articleUuid);
        } else {
            favorited = false;
        }
        favoriteCount = favoriteRepository.countByArticleId(articleUuid);
        return FavoriteResponseDTO.builder().favorited(favorited).favoriteCount(favoriteCount).build();
    }

    @Transactional(readOnly = true)
    public java.util.List<UserArticleDTO> getUserFavorites(String userId) {
        UUID userUuid = UUID.fromString(userId);
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userUuid);
        
        return favorites.stream().map(favorite -> {
            UserArticleDTO dto = UserArticleDTO.builder()
                    .id(favorite.getId().toString())
                    .articleId(favorite.getArticleId().toString())
                    .createdAt(favorite.getCreatedAt().toString())
                    .build();
            
            try {
                ArticleClient.ArticleInfo articleInfo = articleClient.getArticleInfo(favorite.getArticleId().toString());
                if (articleInfo != null) {
                    dto.setArticleTitle(articleInfo.getTitle());
                    dto.setArticleSlug(articleInfo.getSlug());
                }
            } catch (Exception e) {
                log.warn("Failed to get article info for favorite {}: {}", favorite.getId(), e.getMessage());
            }
            
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUserFavorites(String userId) {
        UUID userUuid = UUID.fromString(userId);
        return favoriteRepository.countByUserId(userUuid);
    }
}
