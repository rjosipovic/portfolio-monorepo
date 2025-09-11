package com.playground.gamification_manager.game.service.interfaces;

import com.playground.gamification_manager.game.service.model.LeaderBoardItem;

import java.util.List;

public interface LeaderBoardService {

    List<LeaderBoardItem> getLeaderBoard();
}
