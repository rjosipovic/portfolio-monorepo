package com.playground.gamification_manager.game.dataaccess.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@RequiredArgsConstructor
public class UserScore {

    private final UUID userId;
    private final long totalScore;
}
