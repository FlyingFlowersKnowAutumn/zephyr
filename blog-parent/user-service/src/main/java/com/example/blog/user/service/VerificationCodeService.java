package com.example.blog.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    /** 安全随机源，不能用 java.util.Random */
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String CODE_PREFIX = "code:email:";
    private static final String IP_PREFIX = "code:ip:";

    private static final long CODE_EXPIRE_SECONDS = 5 * 60;
    private static final long SEND_INTERVAL_SECONDS = 60;
    private static final long DAILY_LIMIT = 10;
    private static final long IP_MINUTE_LIMIT = 5;
    private static final long MAX_VERIFY_ATTEMPTS = 5;

    public boolean sendCode(String email, String type) {
        return sendCode(email, type, "unknown");
    }

    public boolean sendCode(String email, String type, String clientIp) {
        String key = CODE_PREFIX + email;

        if (!allowByIp(clientIp)) {
            log.warn("Verification code request too frequent for ip: {}", clientIp);
            return false;
        }

        String lastSend = (String) redisTemplate.opsForValue().get(key + ":lastSend");
        if (lastSend != null && System.currentTimeMillis() - Long.parseLong(lastSend) < SEND_INTERVAL_SECONDS * 1000) {
            log.warn("Verification code request too frequent for: {}", email);
            return false;
        }

        Object dailyCountObj = redisTemplate.opsForValue().get(key + ":daily");
        int count = dailyCountObj != null ? Integer.parseInt(dailyCountObj.toString()) : 0;
        if (count >= DAILY_LIMIT) {
            log.warn("Daily verification code limit exceeded for: {}", email);
            return false;
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.delete(key + ":attempts");
        redisTemplate.opsForValue().set(key + ":lastSend",
                String.valueOf(System.currentTimeMillis()), 24 * 60 * 60, TimeUnit.SECONDS);

        Long daily = redisTemplate.opsForValue().increment(key + ":daily");
        if (daily != null && daily == 1L) {
            // 只在首次自增时设置 24h 过期，避免每次发送都把计数窗口重置
            redisTemplate.expire(key + ":daily", 24 * 60 * 60, TimeUnit.SECONDS);
        }

        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            log.error("Failed to send verification code to {}", email, e);
            // 发信失败要让调用方知道，并作废这个码，不占用用户的日配额
            redisTemplate.delete(key);
            return false;
        }

        log.info("Verification code sent to: {}", email);
        return true;
    }

    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String attemptKey = key + ":attempts";

        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            return false;
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(attemptKey, CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
        if (attempts != null && attempts > MAX_VERIFY_ATTEMPTS) {
            // 超过尝试次数直接作废，强制重新获取，避免 6 位码被暴力穷举
            redisTemplate.delete(key);
            redisTemplate.delete(attemptKey);
            log.warn("Verification code attempts exceeded for: {}", email);
            return false;
        }

        return stored.toString().equals(code);
    }

    public void deleteCode(String email) {
        redisTemplate.delete(CODE_PREFIX + email);
        redisTemplate.delete(CODE_PREFIX + email + ":attempts");
    }

    private boolean allowByIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank() || "unknown".equals(clientIp)) {
            return true;
        }
        String ipKey = IP_PREFIX + clientIp;
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);
        if (ipCount != null && ipCount == 1L) {
            redisTemplate.expire(ipKey, 60, TimeUnit.SECONDS);
        }
        return ipCount == null || ipCount <= IP_MINUTE_LIMIT;
    }
}
