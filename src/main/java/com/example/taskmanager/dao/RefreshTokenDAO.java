package com.example.taskmanager.dao;

import com.example.taskmanager.model.RefreshToken;
import com.example.taskmanager.repository.RefreshTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RefreshTokenDAO {
    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    public Optional<RefreshToken> getRefreshTokenByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    public Optional<RefreshToken> getRefreshTokenByTokenFamily(String tokenFamily) {
        return refreshTokenRepo.findByTokenFamily(tokenFamily);
    }

    public List<RefreshToken> getRefreshTokensBySessionId(String sessionId) {
        return refreshTokenRepo.findBySessionId(sessionId);
    }

    public RefreshToken saveRefreshToken(RefreshToken refreshToken) {
        return refreshTokenRepo.save(refreshToken);
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepo.delete(refreshToken);
    }

    public void deleteRefreshTokenBySessionId(String sessionId) {
        refreshTokenRepo.deleteBySessionId(sessionId);
    }

    public void deleteRefreshTokenByTokenFamily(String tokenFamily) {
        refreshTokenRepo.deleteByTokenFamily(tokenFamily);
    }
}
