package com.playground.gamification_manager.game.dataaccess.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class UserScore {

    private UUID userId;
    private long totalScore;
}
