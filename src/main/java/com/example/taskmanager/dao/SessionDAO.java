package com.example.taskmanager.dao;

import com.example.taskmanager.model.Session;
import com.example.taskmanager.repository.SessionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SessionDAO {
    @Autowired
    private SessionRepo sessionRepo;

    public Session saveSession(Session session) {
        return sessionRepo.save(session);
    }

    public Optional<Session> getSessionById(String sessionId) {
        return sessionRepo.findByIdAndActiveTrue(sessionId);
    }

    public List<Session> getActiveSessionsByUserId(String userId) {
        return sessionRepo.findByUserIdAndActiveTrue(userId);
    }

    public Optional<Session> getSessionByTokenFamily(String tokenFamily) {
        return sessionRepo.findByTokenFamily(tokenFamily);
    }

    public void deleteSession(Session session) {
        sessionRepo.delete(session);
    }

    public void deleteAllUserSessions(String userId) {
        sessionRepo.deleteByUserId(userId);
    }
}
