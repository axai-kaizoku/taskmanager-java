package com.example.taskmanager.controller;

import com.example.taskmanager.dto.*;
import com.example.taskmanager.exception.TokenRefreshException;
import com.example.taskmanager.model.RefreshToken;
import com.example.taskmanager.model.Session;
import com.example.taskmanager.repository.UserRepo;
import com.example.taskmanager.security.JwtUtils;
import com.example.taskmanager.security.UserDetailsImpl;
import com.example.taskmanager.service.RedisAuthService;
import com.example.taskmanager.service.RefreshTokenService;
import com.example.taskmanager.service.SessionService;
import com.example.taskmanager.service.SignupService;
import com.example.taskmanager.util.DeviceInfoExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired AuthenticationManager authenticationManager;
    @Autowired UserRepo userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtils jwtUtils;
    @Autowired RefreshTokenService refreshTokenService;
    @Autowired SessionService sessionService;
    @Autowired SignupService signUpService;
    @Autowired DeviceInfoExtractor deviceInfoExtractor;
    @Autowired RedisAuthService redisAuthService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract device information
        String userAgent = deviceInfoExtractor.getUserAgent(request);
        String deviceInfo = deviceInfoExtractor.extractDeviceInfo(userAgent);
        String ipAddress = deviceInfoExtractor.getClientIp(request);

        // Create session
        Session session = sessionService.createSession(
                userDetails.getId(),
                deviceInfo,
                userAgent,
                ipAddress
        );

        // Generate JWT with session info
        String jwt = jwtUtils.generateTokenFromEmail(userDetails.getEmail());

        // Create refresh token linked to session
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(session);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("User Logged In Successfully",
                new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                        userDetails.getEmail(), roles)));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest,
                                          @RequestParam(value = "referrerUserId", required = false) String referrerUserId) {
        signUpService.signUpUser(signUpRequest,referrerUserId);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully!", null));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(oldToken -> {
                    // Rotate the refresh token (invalidates old, creates new)
                    RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldToken);

                    // Get session to generate new access token
                    Session session = sessionService.getSessionById(newRefreshToken.getSessionId())
                            .orElseThrow(() -> new TokenRefreshException("Session not found or expired"));

                    // Get user from session to generate JWT
                    String userEmail = userRepository.findById(session.getUserId())
                            .orElseThrow(() -> new TokenRefreshException("User not found"))
                            .getEmail();

                    String newAccessToken = jwtUtils.generateTokenFromEmail(userEmail);

                    return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully",
                            new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken())));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String sessionId,
                                     HttpServletRequest request,
                                     Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Verify session belongs to user
        Session session = sessionService.getSessionById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Invalid session"));
        }

        // Revoke the session
        sessionService.revokeSession(sessionId);

        // Delete all refresh tokens for this session
        refreshTokenService.deleteBySessionId(sessionId);

        // Blacklist current access token
        String jwt = parseJwt(request);
        if (jwt != null) {
            try {
                // Calculate remaining TTL based on token expiration
                long expirationMs = jwtUtils.getExpirationFromToken(jwt).getTime() - System.currentTimeMillis();
                if (expirationMs > 0) {
                    redisAuthService.blacklistToken(jwt, java.time.Duration.ofMillis(expirationMs));
                }
            } catch (Exception e) {
                // If we can't parse expiration, blacklist for default duration
                redisAuthService.blacklistToken(jwt, java.time.Duration.ofHours(1));
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
