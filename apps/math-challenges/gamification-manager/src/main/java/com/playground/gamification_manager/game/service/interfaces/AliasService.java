package com.playground.gamification_manager.game.service.interfaces;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AliasService {

    Map<String, String> getAlias(Set<UUID> userIds);
}
