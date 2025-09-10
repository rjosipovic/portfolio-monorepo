package com.playground.gamification_manager.game.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MathUtil {

    public static int getDigitCount(int number) {
        if (number == 0) return 1;
        return (int) Math.log10(Math.abs(number)) + 1;
    }

    public static boolean isPositive(int number) {
        return number > 0;
    }
}
