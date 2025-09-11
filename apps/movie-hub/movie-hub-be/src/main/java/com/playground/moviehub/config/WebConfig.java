package com.playground.moviehub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CorsConfig corsConfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply this rule to all paths
                // === 1. Define allowed origins ===
                .allowedOrigins(corsConfig.getAllowedOrigins())

                // === 2. Define allowed HTTP methods ===
                .allowedMethods(corsConfig.getAllowedMethods())

                // === 3. Define allowed headers ===
                .allowedHeaders(corsConfig.getAllowedHeaders())

                // If your frontend needs to send cookies or use HTTP Basic Auth
                .allowCredentials(false);
    }
}
