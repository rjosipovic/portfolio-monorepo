package com.playground.challenge_manager.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.util.matcher.RequestMatcher;

@RequiredArgsConstructor
public class PortRequestMatcher implements RequestMatcher {

    private final int port;

    @Override
    public boolean matches(HttpServletRequest request) {
        return request.getServerPort() == this.port;
    }
}
