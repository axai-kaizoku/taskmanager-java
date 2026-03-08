package com.example.taskmanager.dao;

import com.example.taskmanager.model.RefreshToken;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.RefreshTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RefreshTokenDAO {
    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    public Optional<RefreshToken> getRefreshTokenByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    public RefreshToken saveRefreshToken(RefreshToken refreshToken) {
        return refreshTokenRepo.save(refreshToken);
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepo.delete(refreshToken);
    }

    public void deleteRefreshTokenByUserId(User user) {
        refreshTokenRepo.deleteByUser(user);
    }
}
