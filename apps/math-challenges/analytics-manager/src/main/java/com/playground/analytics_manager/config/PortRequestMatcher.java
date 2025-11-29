package com.playground.analytics_manager.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class PortRequestMatcher implements RequestMatcher {

    private final int port;

    public PortRequestMatcher(int port) {
        this.port = port;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return request.getServerPort() == this.port;
    }
}