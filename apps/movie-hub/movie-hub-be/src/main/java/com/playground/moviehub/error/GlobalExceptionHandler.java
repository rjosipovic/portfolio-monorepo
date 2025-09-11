package com.playground.moviehub.error;

import com.playground.moviehub.error.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, HttpServletRequest request) {
        var requestId = MDC.get(MDC_REQUEST_ID_KEY);
        // Log the exception with the request ID for correlation
        log.error("Request ID: {} | Unhandled exception occurred for path: {}", requestId, request.getRequestURI(), ex);

        var body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred. Please contact support.");
        body.put("path", request.getRequestURI());
        body.put("requestId", requestId);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        var requestId = MDC.get(MDC_REQUEST_ID_KEY);
        // Log as a warning, since this is a client error (4xx), not a server error (5xx)
        log.warn("Request ID: {} | Resource not found for path: {}. Message: {}", requestId, request.getRequestURI(), ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());
        body.put("requestId", requestId);

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}