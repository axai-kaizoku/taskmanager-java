package com.example.taskmanager.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "refresh_tokens")
public class RefreshToken {
    @Id
    private String id;

    @Indexed
    private String sessionId;

    @Indexed(unique = true)
    private String token;

    private Instant expiryDate;

    // Token family ID for rotation tracking and reuse detection
    @Indexed
    private String tokenFamily;

    // Flag to detect token reuse (security feature)
    private boolean used = false;

    private Instant createdAt;
}
