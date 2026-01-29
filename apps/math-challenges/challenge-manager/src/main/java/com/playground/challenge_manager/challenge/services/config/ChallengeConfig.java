package com.playground.challenge_manager.challenge.services.config;

import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.challenge-generator")
@Data
@Slf4j
public class ChallengeConfig {

    private List<DifficultyLevelConfig> difficultyLevels;

    @PostConstruct
    public void init() {
        if (difficultyLevels != null) {
            for (DifficultyLevelConfig config : difficultyLevels) {
                try {
                    // Find the matching Enum constant
                    DifficultyLevel level = DifficultyLevel.valueOf(config.getLevel().toUpperCase());
                    // Update its configuration
                    level.configure(config.getDigits(), config.getMin(), config.getMax());
                } catch (IllegalArgumentException e) {
                    log.warn("Uname to find matching difficulty level: {}", config.getLevel(), e);
                }
            }
        }
    }

    @Data
    public static class DifficultyLevelConfig {
        private String level;
        private int digits;
        private int min;
        private int max;
    }
}
