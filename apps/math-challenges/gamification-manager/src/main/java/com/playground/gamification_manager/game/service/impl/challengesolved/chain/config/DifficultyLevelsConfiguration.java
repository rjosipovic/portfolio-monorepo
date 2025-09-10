package com.playground.gamification_manager.game.service.impl.challengesolved.chain.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter @Setter
public class DifficultyLevelsConfiguration {

    private List<DifficultyLevelScore> difficultyLevels;

    @Getter
    private Map<String, Integer> scoreMap;
    @Getter
    private Map<Integer, String> difficultyMap;

    @PostConstruct
    private void initMaps() {
        scoreMap = new HashMap<>(difficultyLevels.size());
        difficultyMap = new HashMap<>(difficultyLevels.size());
        for (DifficultyLevelScore difficultyLevelScore : difficultyLevels) {
            scoreMap.put(difficultyLevelScore.getLevel(), difficultyLevelScore.getScore());
            difficultyMap.put(difficultyLevelScore.getDigits(), difficultyLevelScore.getLevel());
        }
    }

    @Getter @Setter
    private static class DifficultyLevelScore {
        private String level;
        private int digits;
        private int score;
    }
}
