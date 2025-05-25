package com.bqs.main.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.redis.enabled:true}")
    private boolean redisEnabled;

    @Value("${app.redis.expiration-seconds:3600}") // Default = 1 hour
    private long expirationSeconds;

    private static final String KEY_PREFIX = "processed_file:";

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isProcessed(String fileKey) {
        if (!redisEnabled) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + fileKey));
    }

    public void markAsProcessed(String fileKey) {
        if (!redisEnabled) return;
        redisTemplate.opsForValue().set(
                KEY_PREFIX + fileKey,
                "DONE",
                Duration.ofSeconds(expirationSeconds)
        );
    }
}
