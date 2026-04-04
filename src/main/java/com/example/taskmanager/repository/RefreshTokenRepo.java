package com.example.taskmanager.repository;

import com.example.taskmanager.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepo extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByTokenFamily(String tokenFamily);
    List<RefreshToken> findBySessionId(String sessionId);
    void deleteBySessionId(String sessionId);
    void deleteByTokenFamily(String tokenFamily);
}
