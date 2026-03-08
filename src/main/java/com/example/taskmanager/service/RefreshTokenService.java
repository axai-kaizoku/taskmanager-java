package com.example.taskmanager.service;

import com.example.taskmanager.dao.RefreshTokenDAO;
import com.example.taskmanager.dao.UserDAO;
import com.example.taskmanager.model.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${app.jwtRefreshExpirationMs:86400000}")
    private Long refreshTokenDurationMs;

    @Autowired private RefreshTokenDAO refreshTokenDAO;
    @Autowired private UserDAO userDAO;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenDAO.getRefreshTokenByToken(token);
    }

    public RefreshToken createRefreshToken(String userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userDAO.getUserById(userId));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenDAO.saveRefreshToken(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenDAO.deleteRefreshToken(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(String userId) {
        refreshTokenDAO.deleteRefreshTokenByUserId(userDAO.getUserById(userId));
        return 1;
    }
}
