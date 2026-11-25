package com.example.blog.interaction.client;

import com.example.blog.common.dto.ArticleStatsUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 同步文章的评论数 / 点赞数到 article-service。
 * 走容器内部服务名直连，不经过网关（因此也不带身份头）。
 */
@Slf4j
@Component
public class ArticleStatsClient {

    private final RestTemplate restTemplate;

    public ArticleStatsClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Value("${article.service.url:http://article-service:8082/api/v1/articles}")
    private String articleServiceUrl;

    public void syncCommentCount(String articleId, int delta) {
        try {
            ArticleStatsUpdateDTO dto = new ArticleStatsUpdateDTO();
            dto.setCommentCountDelta(delta);
            restTemplate.postForEntity(articleServiceUrl + "/" + articleId + "/stats", dto, Void.class);
        } catch (Exception e) {
            log.warn("Failed to sync comment count for article {}: {}", articleId, e.getMessage());
        }
    }

    public void syncLikeCount(String articleId, int delta) {
        try {
            ArticleStatsUpdateDTO dto = new ArticleStatsUpdateDTO();
            dto.setLikeCountDelta(delta);
            restTemplate.postForEntity(articleServiceUrl + "/" + articleId + "/stats", dto, Void.class);
        } catch (Exception e) {
            log.warn("Failed to sync like count for article {}: {}", articleId, e.getMessage());
        }
    }
}
