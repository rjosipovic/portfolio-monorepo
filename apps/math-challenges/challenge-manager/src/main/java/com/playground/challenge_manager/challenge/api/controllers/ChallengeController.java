package com.playground.challenge_manager.challenge.api.controllers;

import com.playground.challenge_manager.challenge.services.interfaces.ChallengeGeneratorService;
import com.playground.challenge_manager.challenge.services.model.Challenge;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeGeneratorService challengeGeneratorService;

    @GetMapping("/random")
    public ResponseEntity<Challenge> get(
            @RequestParam(name = "difficulty", defaultValue = "medium")
            @Pattern(regexp = "easy|medium|hard|expert") String difficulty
    ) {
        return ResponseEntity.ok(challengeGeneratorService.randomChallenge(difficulty));
    }
}
