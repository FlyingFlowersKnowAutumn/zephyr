package com.example.blog.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    private static final String CODE_PREFIX = "code:email:";
    private static final long CODE_EXPIRE = 5 * 60;
    private static final long RATE_LIMIT = 60;
    private static final int DAILY_LIMIT = 40;

    public boolean sendCode(String email, String type) {
        String key = CODE_PREFIX + email;
        
        String lastSend = (String) redisTemplate.opsForValue().get(key + ":lastSend");
        if (lastSend != null && System.currentTimeMillis() - Long.parseLong(lastSend) < RATE_LIMIT * 1000) {
            log.warn("Verification code request too frequent for: {}", email);
            return false;
        }

        Object dailyCountObj = redisTemplate.opsForValue().get(key + ":daily");
        int count = dailyCountObj != null ? Integer.parseInt(dailyCountObj.toString()) : 0;
        if (count >= DAILY_LIMIT) {
            log.warn("Daily verification code limit exceeded for: {}", email);
            return false;
        }

        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(key + ":lastSend", String.valueOf(System.currentTimeMillis()), 24 * 60 * 60, TimeUnit.SECONDS);
        redisTemplate.opsForValue().increment(key + ":daily");
        redisTemplate.expire(key + ":daily", 24 * 60 * 60, TimeUnit.SECONDS);

        emailService.sendVerificationCode(email, code);
        log.info("Verification code sent successfully to: {}", email);
        return true;
    }

    public boolean verifyCode(String email, String code) {
        Object stored = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (stored == null) return false;
        return stored.toString().equals(code);
    }

    public void deleteCode(String email) {
        redisTemplate.delete(CODE_PREFIX + email);
    }
}