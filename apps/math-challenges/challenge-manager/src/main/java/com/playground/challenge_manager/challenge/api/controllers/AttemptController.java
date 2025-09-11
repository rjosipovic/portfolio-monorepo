package com.playground.challenge_manager.challenge.api.controllers;

import com.playground.challenge_manager.auth.JwtUserPrincipal;
import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResultDTO;
import com.playground.challenge_manager.challenge.services.interfaces.AttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService challengeService;

    @PostMapping
    public ResponseEntity<ChallengeResultDTO> verify(
            Authentication authentication,
            @RequestBody @Valid ChallengeAttemptDTO attempt
    ) {
        var principal = (JwtUserPrincipal) authentication.getPrincipal();
        var userId = principal.getClaims().get("userId").toString();
        var attemptWithUserId = attempt.withUserId(userId);
        var result = challengeService.verifyAttempt(attemptWithUserId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ChallengeResultDTO>> findLast10ResultsForUser(Authentication authentication) {
        var principal = (JwtUserPrincipal) authentication.getPrincipal();
        var userId = principal.getClaims().get("userId").toString();
        return ResponseEntity.ok(challengeService.findLast10AttemptsForUser(java.util.UUID.fromString(userId)));
    }
}
