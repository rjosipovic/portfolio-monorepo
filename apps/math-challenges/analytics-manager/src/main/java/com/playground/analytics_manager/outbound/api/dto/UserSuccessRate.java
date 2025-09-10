package com.playground.analytics_manager.outbound.api.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
public class UserSuccessRate {

    private final String alias;
    private final SuccessRate overall;
    private final Map<String, SuccessRate> byGame;
    private final Map<String, SuccessRate> byDifficulty;

    public UserSuccessRate(String alias) {
        this.alias = alias;
        this.overall = new SuccessRate();
        this.byGame = new HashMap<>();
        this.byDifficulty = new HashMap<>();
    }

    public void processSuccessAttempt(String game, String difficulty) {
        this.overall.incrementSuccessAttempt();
        this.byGame.computeIfAbsent(game, k -> new SuccessRate()).incrementSuccessAttempt();
        this.byDifficulty.computeIfAbsent(difficulty, k -> new SuccessRate()).incrementSuccessAttempt();
    }

    public void processFailAttempt(String game, String difficulty) {
        this.overall.incrementFailAttempt();
        this.byGame.computeIfAbsent(game, k -> new SuccessRate()).incrementFailAttempt();
        this.byDifficulty.computeIfAbsent(difficulty, k -> new SuccessRate()).incrementFailAttempt();
    }

    @Data
    @NoArgsConstructor
    public static class SuccessRate {
        private int totalAttempts;
        private int correctAttempts;

        public void incrementSuccessAttempt() {
            this.totalAttempts++;
            this.correctAttempts++;
        }

        public void incrementFailAttempt() {
            this.totalAttempts++;
        }
    }
}
