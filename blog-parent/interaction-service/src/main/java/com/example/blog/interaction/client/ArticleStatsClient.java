package com.example.blog.interaction.client;

import com.example.blog.common.dto.ArticleStatsUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ArticleStatsClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String ARTICLE_SERVICE_URL = "http://localhost:8082/api/v1/articles";

    public void syncCommentCount(String articleId, int delta) {
        try {
            ArticleStatsUpdateDTO dto = new ArticleStatsUpdateDTO();
            dto.setCommentCountDelta(delta);
            restTemplate.postForEntity(ARTICLE_SERVICE_URL + "/" + articleId + "/stats", dto, Void.class);
        } catch (Exception e) {
            log.warn("Failed to sync comment count for article {}: {}", articleId, e.getMessage());
        }
    }

    public void syncLikeCount(String articleId, int delta) {
        try {
            ArticleStatsUpdateDTO dto = new ArticleStatsUpdateDTO();
            dto.setLikeCountDelta(delta);
            restTemplate.postForEntity(ARTICLE_SERVICE_URL + "/" + articleId + "/stats", dto, Void.class);
        } catch (Exception e) {
            log.warn("Failed to sync like count for article {}: {}", articleId, e.getMessage());
        }
    }
}
