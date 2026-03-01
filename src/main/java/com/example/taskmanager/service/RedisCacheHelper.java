package com.example.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheHelper {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Set a value in cache with a TTL in milliseconds.
     */
    public void set(String key, Object value, long ttlMs) {
        redisTemplate.opsForValue().set(key, value, ttlMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Set a value in cache with no expiration.
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Get a value from cache. Returns null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * Delete/Evict a key from cache.
     */
    public boolean evict(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * Check if a key exists in cache.
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Update expiration of a key.
     */
    public boolean expire(String key, long ttlMs) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, ttlMs, TimeUnit.MILLISECONDS));
    }
}
