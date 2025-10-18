package com.playground.gamification_manager.game.service.impl.leaderboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("app.leaderboard-cache")
@Configuration
@Getter @Setter
@NoArgsConstructor
public class LeaderBoardCacheConfiguration {

    private String key;
    private int size;
}
