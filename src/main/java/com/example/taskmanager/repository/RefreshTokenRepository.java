package com.example.taskmanager.repository;

import com.example.taskmanager.model.RefreshToken;
import com.example.taskmanager.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
