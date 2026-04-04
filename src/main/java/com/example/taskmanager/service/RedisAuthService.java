package com.example.taskmanager.service;

import com.example.taskmanager.model.Session;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisAuthService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    private static final String SESSION_PREFIX = "session:";
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String ACCESS_TOKEN_PREFIX = "access:token:";
    private static final String TOKEN_FAMILY_PREFIX = "token:family:";

    public RedisAuthService() {
        this.objectMapper = new ObjectMapper();
//        this.objectMapper.registeredModules(new JavaTimeModule());
    }

    // ============== SESSION CACHING ==============

    public void cacheSession(Session session, Duration ttl) {
        try {
            String key = SESSION_PREFIX + session.getId();
            String sessionJson = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(key, sessionJson, ttl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize session", e);
        }
    }

    public Session getCachedSession(String sessionId) {
        try {
            String key = SESSION_PREFIX + sessionId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return null;
            return objectMapper.readValue(value.toString(), Session.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize session", e);
        }
    }

    public void invalidateSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }

    // ============== TOKEN BLACKLISTING ==============

    /**
     * Blacklist a token (for revoked tokens or used refresh tokens)
     */
    public void blacklistToken(String token, Duration ttl) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "revoked", ttl);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ============== ACCESS TOKEN METADATA ==============

    /**
     * Cache access token metadata (sessionId) for quick validation
     */
    public void cacheAccessToken(String accessToken, String sessionId, Duration ttl) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, sessionId, ttl);
    }

    public String getAccessTokenSessionId(String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void invalidateAccessToken(String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        redisTemplate.delete(key);
    }

    // ============== TOKEN FAMILY TRACKING ==============

    /**
     * Track token family for reuse detection
     * Stores: tokenFamily -> latest refresh token ID
     */
    public void updateTokenFamily(String tokenFamily, String refreshTokenId) {
        String key = TOKEN_FAMILY_PREFIX + tokenFamily;
        redisTemplate.opsForValue().set(key, refreshTokenId, Duration.ofDays(90));
    }

    public String getLatestTokenInFamily(String tokenFamily) {
        String key = TOKEN_FAMILY_PREFIX + tokenFamily;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void invalidateTokenFamily(String tokenFamily) {
        String key = TOKEN_FAMILY_PREFIX + tokenFamily;
        redisTemplate.delete(key);
    }
}
