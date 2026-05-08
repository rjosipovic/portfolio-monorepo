package com.playground.gamification_manager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

    @Bean("loadBalancedWebClientBuilder")
    @LoadBalanced
    @ConditionalOnProperty(name = "app.clients.user-manager.load-balanced", havingValue = "true", matchIfMissing = true)
    public WebClient.Builder loadBalancedWebClientBuilder() {
        log.info("Creating load balanced WebClient");
        return WebClient.builder();
    }

    @Bean("loadBalancedWebClientBuilder")
    @Primary
    @ConditionalOnProperty(name = "app.clients.user-manager.load-balanced", havingValue = "false")
    public WebClient.Builder standardWebClientBuilder() {
        log.info("Creating standard WebClient");
        return WebClient.builder();
    }
}
