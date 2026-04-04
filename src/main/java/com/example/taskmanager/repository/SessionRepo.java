package com.example.taskmanager.repository;

import com.example.taskmanager.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepo extends MongoRepository<Session, String> {
    Optional<Session> findByIdAndActiveTrue(String id);
    List<Session> findByUserIdAndActiveTrue(String userId);
    Optional<Session> findByTokenFamily(String tokenFamily);
    void deleteByUserId(String userId);
}
