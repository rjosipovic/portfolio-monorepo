package com.playground.user_manager.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Wrap the request to allow the body to be read multiple times
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        // Log headers and path as before
        log.info("--- INTERCEPTED REQUEST ---");
        log.info("METHOD: {}", wrappedRequest.getMethod());
        log.info("PATH: {}", wrappedRequest.getRequestURI());
        log.info("--- HEADERS ---");
        Collections.list(wrappedRequest.getHeaderNames()).forEach(headerName ->
                log.info("{}: {}", headerName, wrappedRequest.getHeader(headerName))
        );
        log.info("--- END OF HEADERS ---");

        // Proceed with the filter chain
        filterChain.doFilter(wrappedRequest, response);

        // After the request has been processed, log the body
        byte[] content = wrappedRequest.getContentAsByteArray();
        if (content.length > 0) {
            String requestBody = new String(content, StandardCharsets.UTF_8);
            log.info("--- REQUEST BODY ---");
            log.info(requestBody);
            log.info("--- END OF REQUEST BODY ---");
        } else {
            log.warn("--- REQUEST BODY IS EMPTY ---");
        }
    }
}