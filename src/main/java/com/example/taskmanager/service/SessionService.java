package com.example.taskmanager.service;

import com.example.taskmanager.dao.SessionDAO;
import com.example.taskmanager.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    @Autowired
    private SessionDAO sessionDAO;

    @Autowired
    private RedisAuthService redisAuthService;

    /**
     * Create a new session for a user login
     */
    public Session createSession(String userId, String deviceInfo, String userAgent, String ipAddress) {
        Session session = new Session();
        session.setUserId(userId);
        session.setDeviceInfo(deviceInfo);
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        session.setCreatedAt(Instant.now());
        session.setLastAccessedAt(Instant.now());
        session.setActive(true);
        session.setTokenFamily(UUID.randomUUID().toString());

        session = sessionDAO.saveSession(session);

        // Cache session in Redis
        redisAuthService.cacheSession(session, Duration.ofDays(90));

        return session;
    }

    /**
     * Get session by ID (checks cache first, then DB)
     */
    public Optional<Session> getSessionById(String sessionId) {
        // Try cache first
        Session cached = redisAuthService.getCachedSession(sessionId);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Fallback to database
        Optional<Session> session = sessionDAO.getSessionById(sessionId);
        session.ifPresent(s -> redisAuthService.cacheSession(s, Duration.ofDays(90)));
        return session;
    }

    /**
     * Update session last accessed time
     */
    public void updateSessionAccess(String sessionId) {
        getSessionById(sessionId).ifPresent(session -> {
            session.setLastAccessedAt(Instant.now());
            sessionDAO.saveSession(session);
            redisAuthService.cacheSession(session, Duration.ofDays(90));
        });
    }

    /**
     * Get all active sessions for a user
     */
    public List<Session> getActiveUserSessions(String userId) {
        return sessionDAO.getActiveSessionsByUserId(userId);
    }

    /**
     * Revoke a specific session
     */
    public void revokeSession(String sessionId) {
        getSessionById(sessionId).ifPresent(session -> {
            session.setActive(false);
            sessionDAO.saveSession(session);
            redisAuthService.invalidateSession(sessionId);
        });
    }

    /**
     * Revoke all user sessions except the current one
     */
    public void revokeOtherSessions(String userId, String currentSessionId) {
        List<Session> sessions = getActiveUserSessions(userId);
        sessions.stream()
                .filter(s -> !s.getId().equals(currentSessionId))
                .forEach(s -> revokeSession(s.getId()));
    }

    /**
     * Revoke all user sessions
     */
    public void revokeAllUserSessions(String userId) {
        List<Session> sessions = getActiveUserSessions(userId);
        sessions.forEach(s -> revokeSession(s.getId()));
    }
}
