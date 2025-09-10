package com.playground.analytics_manager.outbound.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticsUpdate {

    String userId;
    String game;
    String difficulty;
    Boolean success;
}
