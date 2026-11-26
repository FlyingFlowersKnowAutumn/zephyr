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
 *
 * <p>传的是明细表算出来的 <b>绝对总数</b>，不是增量 —— 这样同步是幂等的，
 * 任何一次网络失败都会在下一次操作时被自动纠正（见 ArticleStatsUpdateDTO 的注释）。
 * 因此这里 catch 住异常只记日志即可，不需要重试队列或对账补偿。
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

    /**
     * @param totalCount 该文章当前的真实评论总数（不是本次增减量）
     */
    public void syncCommentCount(String articleId, long totalCount) {
        ArticleStatsUpdateDTO dto = new ArticleStatsUpdateDTO();
        dto.setCommentCount(toIntCount(totalCount));
        post(articleId, dto, "comment");
    }

    /**
     * @param totalCount 该文章当前的真实点赞总数（不是本次增减量）
     */
    public void syncLikeCount(String articleId, long totalCount) {
        ArticleStatsUpdateDTO dto = new ArticleStatsUpdateDTO();
        dto.setLikeCount(toIntCount(totalCount));
        post(articleId, dto, "like");
    }

    private void post(String articleId, ArticleStatsUpdateDTO dto, String what) {
        try {
            restTemplate.postForEntity(articleServiceUrl + "/" + articleId + "/stats", dto, Void.class);
        } catch (Exception e) {
            // 失败不致命：下次任何一次互动都会带着新的总值再同步一次
            log.warn("Failed to sync {} count for article {} (value={}): {}",
                    what, articleId, dto.getCommentCount() != null ? dto.getCommentCount() : dto.getLikeCount(),
                    e.getMessage());
        }
    }

    private static int toIntCount(long total) {
        if (total < 0) {
            return 0;
        }
        return (int) Math.min(total, Integer.MAX_VALUE);
    }
}
