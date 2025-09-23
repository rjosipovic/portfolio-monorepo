package com.playground.gamification_manager.game.service.impl.leaderboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@ConfigurationProperties("app.alias-cache")
@Configuration
@Getter
@Setter
@NoArgsConstructor
public class AliasCacheConfiguration {

    private String key = "user_aliases";
    private Duration duration = Duration.ofHours(1);
}
