package com.playground.challenge_manager.challenge.api.controllers;

import com.playground.challenge_manager.challenge.api.ApiPaths;
import com.playground.challenge_manager.challenge.api.dto.AttemptRequest;
import com.playground.challenge_manager.challenge.api.dto.AttemptResponse;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResponse;
import com.playground.challenge_manager.challenge.services.interfaces.ChallengeService;
import com.playground.challenge_manager.challenge.services.interfaces.UserIdentityService;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import com.playground.challenge_manager.challenge.services.model.commands.AttemptVerificationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.ChallengeCreationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.GetChallengeQuery;
import com.playground.challenge_manager.challenge.services.model.commands.SubscribeToChallengeCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserIdentityService userIdentityService;

    /**
     * Creates a new challenge.
     */
    @PostMapping(ApiPaths.CHALLENGES)
    public ResponseEntity<Void> create(
            @RequestParam(name = "difficulty") DifficultyLevel difficulty,
            @RequestParam(name = "operation") OperationType operation,
            @RequestParam(name = "operandCount", required = false, defaultValue = "2") int operandCount
    ) {
        var userId = userIdentityService.getCurrentUserId();

        var createCommand = ChallengeCreationCommand.builder()
                .difficulty(difficulty)
                .operation(operation)
                .operandCount(operandCount)
                .userId(userId)
                .build();

        var challengeId = challengeService.create(createCommand);

        var location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(challengeId)
                .toUri();

        var relativeLocation = URI.create(location.getPath());
        return ResponseEntity.accepted().location(relativeLocation).build();
    }

    /**
     * Retrieves the current state and details of a challenge.
     * Used by the client to poll for status changes and get challenge data.
     */
    @GetMapping(ApiPaths.CHALLENGES_WITH_ID)
    public ResponseEntity<ChallengeResponse> getChallenge(@PathVariable("id") UUID challengeId) {
        var userId = userIdentityService.getCurrentUserId();
        var query = GetChallengeQuery.builder()
                .challengeId(challengeId)
                .userId(userId)
                .build();
        var challenge = challengeService.getChallenge(query);
        return ResponseEntity.ok(challenge);
    }

    /**
     * Subscribes to Server-Sent Events for a specific challenge.
     * The client will receive an event when the challenge is ready.
     */
    @GetMapping(value = ApiPaths.CHALLENGE_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChallenge(@PathVariable("id") UUID challengeId) {
        var userId = userIdentityService.getCurrentUserId();
        var subscribeToChallengeCommand = SubscribeToChallengeCommand.builder()
                .challengeId(challengeId)
                .userId(userId)
                .build();
        return challengeService.subscribeToChallenge(subscribeToChallengeCommand);
    }

    /**
     * Submits an attempt for a challenge.
     * The service layer will handle state and expiration checks.
     */
    @PostMapping(ApiPaths.CHALLENGE_ATTEMPT)
    public ResponseEntity<AttemptResponse> submitAttempt(
            @PathVariable("id") UUID challengeId,
            @Valid @RequestBody AttemptRequest attemptRequest
    ) {
        var userId = userIdentityService.getCurrentUserId();
        var guess = attemptRequest.getGuess();
        var command = AttemptVerificationCommand.builder()
                .challengeId(challengeId)
                .userId(userId)
                .guess(guess)
                .build();
        var attemptResponse = challengeService.submitAttempt(command);
        return ResponseEntity.ok(attemptResponse);
    }
}
