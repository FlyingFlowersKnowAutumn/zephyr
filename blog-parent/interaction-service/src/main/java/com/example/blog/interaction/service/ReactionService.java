package com.example.blog.interaction.service;

import com.example.blog.common.dto.ReactionResponseDTO;
import com.example.blog.interaction.client.ArticleStatsClient;
import com.example.blog.interaction.entity.Reaction;
import com.example.blog.interaction.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final ArticleStatsClient articleStatsClient;

    @Transactional
    public ReactionResponseDTO toggleReaction(String articleId, String userId) {
        Optional<Reaction> existing = reactionRepository.findByUserIdAndArticleId(userId, articleId);

        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            long count = reactionRepository.countByArticleId(articleId);
            articleStatsClient.syncLikeCount(articleId, -1);
            log.info("Reaction removed: articleId={}, userId={}", articleId, userId);
            return ReactionResponseDTO.builder().liked(false).likeCount(count).build();
        } else {
            Reaction reaction = Reaction.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .build();
            reactionRepository.save(reaction);
            long count = reactionRepository.countByArticleId(articleId);
            articleStatsClient.syncLikeCount(articleId, 1);
            log.info("Reaction added: articleId={}, userId={}", articleId, userId);
            return ReactionResponseDTO.builder().liked(true).likeCount(count).build();
        }
    }

    @Transactional(readOnly = true)
    public ReactionResponseDTO getReactionStatus(String articleId, String userId) {
        boolean liked = reactionRepository.existsByUserIdAndArticleId(userId, articleId);
        long likeCount = reactionRepository.countByArticleId(articleId);
        return ReactionResponseDTO.builder().liked(liked).likeCount(likeCount).build();
    }
}
