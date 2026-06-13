package com.studioengine.tutor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.cleanup")
public class CleanupProperties {

    private Duration loginAttemptRetention;
    private Duration otpRetention;
    private Duration notificationLogRetention;
    private Duration stateLogRetention;
    private boolean stateLogEnabled;
}
