package com.playground.user_manager.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.util.matcher.RequestMatcher;

@RequiredArgsConstructor
public class PortRequestMatcher implements RequestMatcher {

    private final int port;

    /**
     * Determines if the incoming request was received on the configured port.
     * @param request The request to check.
     * @return {@code true} if the request's server port matches the configured port, {@code false} otherwise.
     */
    @Override
    public boolean matches(HttpServletRequest request) {
        return request.getServerPort() == port;
    }
}
