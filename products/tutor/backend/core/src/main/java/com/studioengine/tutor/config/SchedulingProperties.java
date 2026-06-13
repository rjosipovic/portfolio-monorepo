package com.studioengine.tutor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.scheduling")
public class SchedulingProperties {

    private Duration reservationTimeout;
    private Duration paymentOverdueThreshold;
    private Duration cancellationDeadline;
    private Duration nudgeDelay;
    private Duration nudgeCooldown;
}
