package com.playground.api_gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final CorsConfig corsConfig;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(corsConfig.getAllowedOrigins())
                        .allowedMethods(corsConfig.getAllowedMethods())
                        .allowedHeaders(corsConfig.getAllowedHeaders())
                        .allowCredentials(false);
            }
        };
    }
}
