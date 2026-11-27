package com.example.blog.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败锁定（防暴力破解）。
 *
 * <p>两个维度各自独立计数，任一维度超限即锁定：
 * <ul>
 *   <li><b>IP</b>：默认 5 次 / 5 分钟 —— 挡住单机反复试密码；</li>
 *   <li><b>账号</b>：默认 10 次 / 15 分钟 —— 挡住换 IP 打同一个账号。</li>
 * </ul>
 *
 * <p>检查放在 BCrypt 校验<b>之前</b>：BCrypt 每次要 60~100ms CPU，
 * 先查计数可以让被锁的请求直接返回，不消耗 CPU。
 *
 * <p>Redis 异常时一律放行（fail-open）并打 warn：
 * 登录是核心功能，宁可短暂失去保护，也不能因为缓存抖动让所有人登不进来。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginGuardService {

    private static final String IP_KEY_PREFIX = "auth:login-fail:ip:";
    private static final String USER_KEY_PREFIX = "auth:login-fail:user:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${security.login-lock.ip-max-failures:5}")
    private int ipMaxFailures;

    @Value("${security.login-lock.ip-lock-minutes:5}")
    private long ipLockMinutes;

    @Value("${security.login-lock.user-max-failures:10}")
    private int userMaxFailures;

    @Value("${security.login-lock.user-lock-minutes:15}")
    private long userLockMinutes;

    /**
     * 还要锁多久（分钟）。返回 0 表示未被锁定，可以继续尝试。
     */
    public long lockRemainingMinutes(String clientIp, String account) {
        try {
            long ipRemaining = remainingIfOverLimit(
                    IP_KEY_PREFIX + normalize(clientIp), ipMaxFailures);
            long accountRemaining = remainingIfOverLimit(
                    USER_KEY_PREFIX + normalize(account), userMaxFailures);
            return Math.max(ipRemaining, accountRemaining);
        } catch (Exception e) {
            log.warn("登录锁定状态读取失败，本次放行: ip={}, error={}", clientIp, e.getMessage());
            return 0L;
        }
    }

    /**
     * 记一次失败。达到上限后剩余时间由 {@link #lockRemainingMinutes} 判定。
     */
    public void recordFailure(String clientIp, String account) {
        try {
            increase(IP_KEY_PREFIX + normalize(clientIp), ipLockMinutes);
            increase(USER_KEY_PREFIX + normalize(account), userLockMinutes);
        } catch (Exception e) {
            log.warn("登录失败计数写入失败: ip={}, error={}", clientIp, e.getMessage());
        }
    }

    /**
     * 登录成功后清零，避免"以前错过的几次"累积到下次登录。
     */
    public void clearFailures(String clientIp, String account) {
        try {
            redisTemplate.delete(IP_KEY_PREFIX + normalize(clientIp));
            redisTemplate.delete(USER_KEY_PREFIX + normalize(account));
        } catch (Exception e) {
            log.warn("登录失败计数清理失败: ip={}, error={}", clientIp, e.getMessage());
        }
    }

    private long remainingIfOverLimit(String key, int maxFailures) {
        long failures = readCount(key);
        if (failures < maxFailures) {
            return 0L;
        }
        Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttlSeconds == null || ttlSeconds <= 0) {
            return 1L;
        }
        return (ttlSeconds + 59) / 60;
    }

    private void increase(String key, long ttlMinutes) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, ttlMinutes, TimeUnit.MINUTES);
        }
    }

    private long readCount(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim().toLowerCase();
    }
}
