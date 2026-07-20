package com.example.blog.article.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagVisitService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_VISIT_PREFIX = "tag:visit:user:";
    private static final long USER_VISIT_TTL_HOURS = 1;

    public boolean recordVisit(String tagId, String userId) {
        if (tagId == null || tagId.isBlank()) {
            return false;
        }

        String userKey = USER_VISIT_PREFIX + userId + ":" + tagId;

        Boolean existed = redisTemplate.opsForValue().setIfAbsent(userKey, "1", USER_VISIT_TTL_HOURS, TimeUnit.HOURS);

        if (existed == null) {
            log.warn("Redis operation returned null for userKey: {}", userKey);
            return true;
        }

        if (!existed) {
            log.debug("User already visited tag within TTL: userId={}, tagId={}", userId, tagId);
            return false;
        }

        log.debug("Tag visit recorded (first visit in TTL): tagId={}, userId={}", tagId, userId);
        return true;
    }

    public boolean recordVisit(String tagId) {
        return recordVisit(tagId, "anonymous");
    }
}