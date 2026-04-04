package com.example.taskmanager.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "sessions")
public class Session {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String deviceInfo;
    private String userAgent;
    private String ipAddress;

    private Instant createdAt;
    private Instant lastAccessedAt;

    // Flag to track if session is still active
    private boolean active = true;

    // Token family ID for rotation tracking
    private String tokenFamily;
}
