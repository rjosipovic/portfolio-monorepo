package com.playground.analytics_manager.outbound.api.controllers;

import com.playground.analytics_manager.outbound.api.dto.StatisticsUpdate;
import com.playground.analytics_manager.outbound.api.dto.UserSuccessRate;
import com.playground.analytics_manager.outbound.auth.JwtUserPrincipal;
import com.playground.analytics_manager.outbound.services.user_statistics.UserStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final UserStatisticsService userStatisticsService;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public StatisticsController(UserStatisticsService userStatisticsService) {
        this.userStatisticsService = userStatisticsService;
    }

    @GetMapping("/user")
    public ResponseEntity<UserSuccessRate> get(Authentication authentication) {
        var principal = (JwtUserPrincipal) authentication.getPrincipal();
        var userId = principal.getClaims().get("userId").toString();
        var userStatistics = userStatisticsService.getUserStatistics(userId);
        return ResponseEntity.ok(userStatistics);
    }

    @GetMapping("/user/subscribe")
    public SseEmitter emmit(Authentication authentication) {
        var principal = (JwtUserPrincipal) authentication.getPrincipal();
        var userId = principal.getClaims().get("userId").toString();
        var emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        return emitter;
    }

    public void publishStatisticsUpdate(StatisticsUpdate statisticsUpdate) {
        var userId = statisticsUpdate.getUserId();
        var emitter = emitters.get(statisticsUpdate.getUserId());

        if (emitter != null) {
            try {
                emitter.send(statisticsUpdate);
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(userId);
            }
        }
    }
}
