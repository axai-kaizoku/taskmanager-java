package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.SessionDTO;
import com.example.taskmanager.model.Session;
import com.example.taskmanager.security.UserDetailsImpl;
import com.example.taskmanager.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * Get all active sessions for the current user
     */
    @GetMapping
    public ResponseEntity<?> getActiveSessions(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        List<Session> sessions = sessionService.getActiveUserSessions(userId);

        List<SessionDTO> sessionDTOs = sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Active sessions retrieved", sessionDTOs));
    }

    /**
     * Revoke a specific session
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> revokeSession(@PathVariable String sessionId,
                                            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        // Verify session belongs to user
        Session session = sessionService.getSessionById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You can only revoke your own sessions"));
        }

        sessionService.revokeSession(sessionId);

        return ResponseEntity.ok(ApiResponse.success("Session revoked successfully", null));
    }

    /**
     * Revoke all sessions except the current one
     */
    @PostMapping("/revoke-others")
    public ResponseEntity<?> revokeOtherSessions(@RequestParam String currentSessionId,
                                                  Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        // Verify current session belongs to user
        Session session = sessionService.getSessionById(currentSessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Invalid session"));
        }

        sessionService.revokeOtherSessions(userId, currentSessionId);

        return ResponseEntity.ok(ApiResponse.success("Other sessions revoked successfully", null));
    }

    /**
     * Revoke ALL sessions (logout from all devices)
     */
    @PostMapping("/revoke-all")
    public ResponseEntity<?> revokeAllSessions(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        sessionService.revokeAllUserSessions(userId);

        return ResponseEntity.ok(ApiResponse.success("All sessions revoked successfully", null));
    }

    private SessionDTO convertToDTO(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.setSessionId(session.getId());
        dto.setDeviceInfo(session.getDeviceInfo());
        dto.setIpAddress(session.getIpAddress());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setLastAccessedAt(session.getLastAccessedAt());
        dto.setActive(session.isActive());
        return dto;
    }
}
