package com.playground.analytics_manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "management")
@Getter
@Setter
@Configuration
public class ManagementConfig {

    private Server server;

    @Getter @Setter
    public static class Server {
        private int port;
    }
}