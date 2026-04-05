package com.example.taskmanager.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class SessionDTO {
    private String sessionId;
    private String deviceInfo;
    private String ipAddress;
    private Instant createdAt;
    private Instant lastAccessedAt;
    private boolean active;
}
