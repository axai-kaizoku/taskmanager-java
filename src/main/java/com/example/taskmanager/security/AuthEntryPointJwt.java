package com.example.taskmanager.security;

import com.example.taskmanager.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = (String) request.getAttribute("auth_error");
        if (message == null) {
            message = "Unauthorized access";
        }

        ApiResponse<Void> apiResponse = ApiResponse.error(message);

        final tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
        mapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
