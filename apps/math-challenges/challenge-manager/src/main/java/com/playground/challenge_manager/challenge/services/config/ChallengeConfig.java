package com.playground.challenge_manager.challenge.services.config;

import com.playground.challenge_manager.challenge.services.impl.ChallengeGeneratorServiceImpl;
import com.playground.challenge_manager.challenge.services.interfaces.ChallengeGeneratorService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Configuration
@ConfigurationProperties(prefix = "app.challenge-generator")
@Getter
@Setter
public class ChallengeConfig {

    private List<DifficultyLevel> difficultyLevels;

    @Bean
    public ChallengeGeneratorService challengeGeneratorService() {
        var difficultyRangeMap = new HashMap<String, Pair<Integer, Integer>>();
        difficultyLevels.forEach(difficulty -> difficultyRangeMap.put(difficulty.getLevel(), Pair.of(difficulty.getMin(), difficulty.getMax())));
        return new ChallengeGeneratorServiceImpl(new Random(), difficultyRangeMap);
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyLevel {
        private String level;
        private int min;
        private int max;
    }
}
