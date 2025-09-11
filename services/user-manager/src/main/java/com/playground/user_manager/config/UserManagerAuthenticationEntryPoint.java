package com.playground.user_manager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.user_manager.errors.custom.UserManagerError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// This custom AuthenticationEntryPoint is invoked when an unauthenticated user
// attempts to access a protected resource. It sends a 401 Unauthorized response
// with a structured JSON body.
@Component
@RequiredArgsConstructor
public class UserManagerAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // Set the HTTP status to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Set the content type to application/json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Create a custom error response object
        var message = "Authentication required";
        var code = HttpStatus.UNAUTHORIZED.getReasonPhrase();
        var reason = "Authentication failed: Invalid or missing token";
        var apiError = new UserManagerError(message, code, reason);

        // Write the JSON error response to the output stream
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}
