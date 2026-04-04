package com.example.taskmanager.security;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.service.RedisAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private RedisAuthService redisAuthService;

    // Public endpoints that don't require authentication
    private static final String[] PUBLIC_URLS = {
        "/api/auth/signin",
        "/api/auth/signup",
        "/api/auth/refreshtoken"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if this is a public endpoint
        boolean isPublicEndpoint = isPublicEndpoint(request.getRequestURI());

        // For public endpoints, skip authentication entirely
        if (isPublicEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        // For protected endpoints, parse the JWT token
        String jwt = parseJwt(request);

        // No token provided for protected endpoint
        if (jwt == null) {
            // Let it continue - Spring Security's entry point will handle it
            // This allows 404s to be handled properly
            filterChain.doFilter(request, response);
            return;
        }

        // Token provided - validate it
        try {
            // Check if token is blacklisted (revoked)
            if (redisAuthService.isTokenBlacklisted(jwt)) {
                request.setAttribute("auth_error", "Token has been revoked");
                filterChain.doFilter(request, response);
                return;
            }

            // Validate JWT
            if (jwtUtils.validateJwtToken(jwt)) {
                String email = jwtUtils.getEmailFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            request.setAttribute("auth_error", "Token expired");
        } catch (Exception e) {
            request.setAttribute("auth_error", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String uri) {
        for (String publicUrl : PUBLIC_URLS) {
            if (uri.equals(publicUrl)) {
                return true;
            }
        }
        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> apiResponse = ApiResponse.error(message);
        final tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
        mapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
