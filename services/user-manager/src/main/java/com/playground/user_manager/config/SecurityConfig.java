package com.playground.user_manager.config;

import com.playground.user_manager.auth.filters.JwtAuthenticationFilter;
import com.playground.user_manager.auth.service.AuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ManagementConfig managementConfig;
    private final AuthConfig authConfig;
    private final UserManagerAuthenticationEntryPoint userManagerAuthenticationEntryPoint;
    private final UserManagerAccessDeniedHandler userManagerAccessDeniedHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new PortRequestMatcher(managementConfig.getServer().getPort()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/error/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(userManagerAuthenticationEntryPoint)
                        .accessDeniedHandler(userManagerAccessDeniedHandler))
                .addFilterBefore(new JwtAuthenticationFilter(authConfig.getSecret()), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> {
            throw new UnsupportedOperationException("No authentication should be done here.");
        };
    }
}
