package com.playground.user_manager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.user_manager.errors.custom.UserManagerError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// This custom AccessDeniedHandler is invoked when an authenticated user
// attempts to access a resource for which they do not have sufficient permissions.
// It sends a 403 Forbidden response with a structured JSON body.
@Component
@RequiredArgsConstructor
public class UserManagerAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // Set the HTTP status to 403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // Set the content type to application/json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Create a custom error response object
        var message = "Access Denied";
        var code = HttpStatus.FORBIDDEN.getReasonPhrase();
        var reason = "You do not have the required permissions to access this resource.";
        var apiError = new UserManagerError(message, code, reason);

        // Write the JSON error response to the output stream
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}
