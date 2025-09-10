package com.playground.gamification_manager.game.service.impl.challengesolved.chain.config;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ConfigurationProperties("app")
@Configuration
public class BadgesConfiguration {

    @Getter @Setter
    private List<BadgeConfig> badges;

    private Map<BadgeType, BadgeConfig> badgeConfigMap;

    @PostConstruct
    private void initBadgeConfigMap() {
        this.badgeConfigMap = badges.stream()
                .collect(Collectors.toMap(BadgeConfig::getBadgeType, Function.identity()));
    }

    public BadgeConfig getBadgeConfig(BadgeType badgeType) {
        return badgeConfigMap.get(badgeType);
    }

    @Getter
    @AllArgsConstructor
    public static class BadgeConfig {
        private BadgeType badgeType;
        private Integer scoreThreshold;
        private Integer luckyNumber;
    }
}
