package com.playground.analytics_manager.outbound.api.controllers;

import com.playground.analytics_manager.outbound.api.dto.ChallengeResult;
import com.playground.analytics_manager.outbound.auth.JwtUserPrincipal;
import com.playground.analytics_manager.outbound.services.challenge_history.ChallengeHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final ChallengeHistoryService challengeHistoryService;

    @GetMapping
    public ResponseEntity<List<ChallengeResult>> getHistoryAttempts(Authentication authentication) {
        var principal = (JwtUserPrincipal) authentication.getPrincipal();
        var userId = principal.getClaims().get("userId").toString();
        return ResponseEntity.ok(challengeHistoryService.getHistoryAttempts(userId));
    }
}
