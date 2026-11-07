package com.studioengine.tutor.api.security;

import com.studioengine.tutor.config.CorsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    @Mock
    private CorsProperties corsProperties;

    @InjectMocks
    private CorsConfig corsConfig;

    @Test
    void shouldConfigureCorsConfiguration() {
        // given
        var origins = List.of("http://localhost:5500", "http://localhost:5501");
        var methods = List.of("GET", "POST", "PUT", "DELETE");
        var headers = List.of("Authorization", "Content-Type");
        var allowedOrigins = "%s, %s".formatted(origins.getFirst(), origins.getLast());
        var allowedMethods = "%s, %s, %s, %s".formatted(methods.get(0), methods.get(1), methods.get(2), methods.get(3));
        var allowedHeaders = "%s, %s".formatted(headers.getFirst(), headers.getLast());
        when(corsProperties.getAllowedOrigins()).thenReturn(allowedOrigins);
        when(corsProperties.getAllowedHeaders()).thenReturn(allowedHeaders);
        when(corsProperties.getAllowedMethods()).thenReturn(allowedMethods);

        // when
        var configSource = (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
        var configs = configSource.getCorsConfigurations();

        // then
        assertThat(configs).containsKey("/**");
        var corsConfig = configs.get("/**");
        assertThat(corsConfig.getAllowedOrigins()).isEqualTo(origins);
        assertThat(corsConfig.getAllowedMethods()).isEqualTo(methods);
        assertThat(corsConfig.getAllowedHeaders()).isEqualTo(headers);
        assertThat(corsConfig.getAllowCredentials()).isFalse();
    }

    @Test
    void shouldNotConfigureCorsConfigurationWhenOriginsIsNull() {
        // given
        when(corsProperties.getAllowedOrigins()).thenReturn(null);

        // when
        var configSource = (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
        var configs = configSource.getCorsConfigurations();

        // then
        assertThat(configs).doesNotContainKey("/**");
    }

    @Test
    void shouldNotConfigureCorsConfigurationWhenOriginsAreEmpty() {
        // given
        var allowedOrigins = "";
        when(corsProperties.getAllowedOrigins()).thenReturn(allowedOrigins);

        // when
        var configSource = (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
        var configs = configSource.getCorsConfigurations();

        // then
        assertThat(configs).doesNotContainKey("/**");
    }
}