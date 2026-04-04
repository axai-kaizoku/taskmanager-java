package com.example.taskmanager.security;

import com.example.taskmanager.dto.ApiResponse;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final String CONTENT_TYPE = "application/json";

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionConfigurer ->
                        sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandlingConfigurer -> {
                    // Handle access denied (403) - when user doesn't have required role/permission
                    exceptionHandlingConfigurer.accessDeniedHandler((request, response, e) -> {
                        String bearerToken = request.getHeader("Authorization");
                        System.err.println("Access Denied: " + request.getRequestURI() +
                                " | Token: " + bearerToken +
                                " | Error: " + e.getMessage());

                        ApiResponse<Void> apiResponse = ApiResponse.error("Access denied: " + e.getMessage());
                        response.setContentType(CONTENT_TYPE);
                        response.setStatus(403);
                        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                    });

                    // Handle authentication failures (401) - when token is invalid/expired/missing
                    exceptionHandlingConfigurer.authenticationEntryPoint((request, response, e) -> {
                        String bearerToken = request.getHeader("Authorization");
                        String authError = (String) request.getAttribute("auth_error");

                        System.err.println("Authentication Failed: " + request.getRequestURI() +
                                " | Token: " + bearerToken +
                                " | Error: " + (authError != null ? authError : e.getMessage()));

                        ApiResponse<Void> apiResponse;
                        if (authError != null) {
                            apiResponse = ApiResponse.error(authError);
                        } else if (bearerToken == null || bearerToken.isEmpty()) {
                            apiResponse = ApiResponse.error("Authorization header missing. Please provide a valid token");
                        } else {
                            apiResponse = ApiResponse.error("Unauthorized: " + e.getMessage());
                        }

                        response.setContentType(CONTENT_TYPE);
                        response.setStatus(401);
                        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                    });
                })
                .authorizeHttpRequests(authorize ->
                        authorize
                                // Public auth endpoints
                                .requestMatchers("/api/auth/signin").permitAll()
                                .requestMatchers("/api/auth/signup").permitAll()
                                .requestMatchers("/api/auth/refreshtoken").permitAll()

                                // Public endpoints
                                .requestMatchers("/error", "/favicon.ico").permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers("/swagger*/**").permitAll()

                                // All other API routes require authentication
                                .requestMatchers("/api/**").authenticated()

                                // Allow other routes for proper 404 handling
                                .anyRequest().permitAll()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins
        configuration.setAllowedOrigins(List.of(
                "https://axai-kaizoku.github.io"
        ));

        // Allowed methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow credentials (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        // Exposed headers (headers that browser can access)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));

        // Max age for preflight requests (1 hour)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

