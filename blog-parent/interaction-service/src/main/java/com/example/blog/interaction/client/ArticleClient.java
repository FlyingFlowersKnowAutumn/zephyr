package com.example.blog.interaction.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ArticleClient {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${article.service.url:http://localhost:8082/api/v1/articles}")
    private String articleServiceUrl;

    public ArticleInfo getArticleInfo(String articleId) {
        try {
            var response = restTemplate.getForEntity(articleServiceUrl + "/" + articleId, ArticleResponse.class);
            if (response.getBody() != null && response.getBody().getCode() == 0 && response.getBody().getData() != null) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.warn("Failed to get article info for article {}: {}", articleId, e.getMessage());
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleResponse {
        private int code;
        private String message;
        private ArticleInfo data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleInfo {
        private String id;
        private String title;
        private String slug;
    }
}
