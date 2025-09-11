package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedHandler;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.DifficultyLevelsConfiguration;
import com.playground.gamification_manager.game.util.MathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DifficultyHandler implements ChallengeSolvedHandler {

    private final DifficultyLevelsConfiguration difficultyLevelsConfiguration;

    @Override
    public boolean shouldHandle(ChallengeSolvedContext ctx) {
        return ctx.isCorrect();
    }

    @Override
    public void handle(ChallengeSolvedContext ctx) {

        //Expecting first number and second number to have the same number of digits
        var firstNumber = ctx.getFirstNumber();
        var firstDigitsCount = MathUtil.getDigitCount(firstNumber);

        var difficulty = difficultyLevelsConfiguration.getDifficultyMap().get(firstDigitsCount);
        ctx.setDifficulty(difficulty);
    }
}
