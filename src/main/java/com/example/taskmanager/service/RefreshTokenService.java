package com.example.taskmanager.service;

import com.example.taskmanager.dao.RefreshTokenDAO;
import com.example.taskmanager.exception.TokenRefreshException;
import com.example.taskmanager.model.RefreshToken;
import com.example.taskmanager.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${app.jwtRefreshExpirationMs:86400000}")
    private Long refreshTokenDurationMs;

    @Autowired private RefreshTokenDAO refreshTokenDAO;
    @Autowired private SessionService sessionService;
    @Autowired private RedisAuthService redisAuthService;

    public Optional<RefreshToken> findByToken(String token) {
        // Check if token is blacklisted
        if (redisAuthService.isTokenBlacklisted(token)) {
            return Optional.empty();
        }
        return refreshTokenDAO.getRefreshTokenByToken(token);
    }

    /**
     * Create initial refresh token for a new session
     */
    public RefreshToken createRefreshToken(Session session) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setSessionId(session.getId());
        refreshToken.setTokenFamily(session.getTokenFamily());
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setUsed(false);

        refreshToken = refreshTokenDAO.saveRefreshToken(refreshToken);

        // Track this token in the family
        redisAuthService.updateTokenFamily(session.getTokenFamily(), refreshToken.getId());

        return refreshToken;
    }

    /**
     * Rotate refresh token - invalidate old, create new
     * Implements token rotation security pattern
     */
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Verify token hasn't expired
        verifyExpiration(oldToken);

        // SECURITY: Check for token reuse (token rotation attack detection)
        if (oldToken.isUsed()) {
            // Token reuse detected! This is a potential security breach
            // Revoke entire token family (all tokens in this session)
            handleTokenReuseDetected(oldToken);
            throw new TokenRefreshException("Token reuse detected! Session revoked for security.");
        }

        // Mark old token as used
        oldToken.setUsed(true);
        refreshTokenDAO.saveRefreshToken(oldToken);

        // Blacklist old token
        long ttl = oldToken.getExpiryDate().toEpochMilli() - Instant.now().toEpochMilli();
        if (ttl > 0) {
            redisAuthService.blacklistToken(oldToken.getToken(), Duration.ofMillis(ttl));
        }

        // Create new refresh token with same session and token family
        RefreshToken newToken = new RefreshToken();
        newToken.setSessionId(oldToken.getSessionId());
        newToken.setTokenFamily(oldToken.getTokenFamily());
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs)); // Reset expiry
        newToken.setCreatedAt(Instant.now());
        newToken.setUsed(false);

        newToken = refreshTokenDAO.saveRefreshToken(newToken);

        // Update token family tracker
        redisAuthService.updateTokenFamily(newToken.getTokenFamily(), newToken.getId());

        // Update session last accessed time
        sessionService.updateSessionAccess(oldToken.getSessionId());

        return newToken;
    }

    /**
     * Handle token reuse detection - revoke entire session
     */
    private void handleTokenReuseDetected(RefreshToken token) {
        // Revoke the session
        sessionService.revokeSession(token.getSessionId());

        // Invalidate all tokens in this family
        refreshTokenDAO.deleteRefreshTokenByTokenFamily(token.getTokenFamily());
        redisAuthService.invalidateTokenFamily(token.getTokenFamily());
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenDAO.deleteRefreshToken(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token expired. Please login again");
        }

        return token;
    }

    @Transactional
    public void deleteBySessionId(String sessionId) {
        refreshTokenDAO.deleteRefreshTokenBySessionId(sessionId);
    }
}
