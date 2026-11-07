package com.studioengine.tutor.api.security;

import com.studioengine.tutor.config.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private static final String COMMA = ",";

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var source = new UrlBasedCorsConfigurationSource();

        var allowedOrigins = corsProperties.getAllowedOrigins();
        if (Strings.isBlank(allowedOrigins)) {
            // Single-origin deployment: frontend and API share an origin (served behind one reverse proxy)
            // so no cross-origin access is configured. Registering no CORS config means same-origin
            // requests pass normally and cross-origin requests are simply not granted CORS headers
            return source;
        }

        var config = new CorsConfiguration();
        config.setAllowedOrigins(splitString(corsProperties.getAllowedOrigins()));
        config.setAllowedMethods(splitString(corsProperties.getAllowedMethods()));
        config.setAllowedHeaders(splitString(corsProperties.getAllowedHeaders()));
        config.setAllowCredentials(false);

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> splitString(String input) {
        return Arrays.stream(input.split(COMMA))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
