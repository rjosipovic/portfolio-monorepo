package com.playground.challenge_manager.challenge.services.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DifficultyLevel {

    EASY(1, 1, 9),
    MEDIUM(2, 10, 99),
    HARD(3, 100, 999),
    EXPERT(4, 1000, 9999);

    private int digits;
    private int min;
    private int max;

    public void configure(int digits, int min, int max) {
        this.digits = digits;
        this.min = min;
        this.max = max;
    }
}
