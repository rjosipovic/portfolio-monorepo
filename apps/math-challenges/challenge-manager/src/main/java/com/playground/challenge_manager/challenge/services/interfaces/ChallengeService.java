package com.playground.challenge_manager.challenge.services.interfaces;

import com.playground.challenge_manager.challenge.api.dto.AttemptResponse;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResponse;
import com.playground.challenge_manager.challenge.services.model.commands.AttemptVerificationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.ChallengeCreationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.GetChallengeQuery;
import com.playground.challenge_manager.challenge.services.model.commands.SubscribeToChallengeCommand;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface ChallengeService {

    /**
     * Creates a new challenge entity with a 'GENERATED' status and publishes an
     * event to trigger asynchronous processing.
     *
     * @param command The details for the new challenge.
     * @return The ID of the newly created challenge.
     */
    UUID create(ChallengeCreationCommand command);

    /**
     * Retrieves the current state and details of a specific challenge.
     *
     * @param query The query object containing the challengeId and userId.
     * @return A response DTO representing the challenge's current state and data.
     */
    ChallengeResponse getChallenge(GetChallengeQuery query);

    /**
     * Submits a user's guess for a challenge and returns the result.
     * This method performs validation (state, expiration) and updates the challenge status.
     *
     * @param command The command object containing all data for the attempt (challengeId, userId, guess).
     * @return A response DTO with the result of the attempt (correct/incorrect) and the new status.
     */
    AttemptResponse submitAttempt(AttemptVerificationCommand command);

    /**
     * Subscribes a client to receive Server-Sent Events for a specific challenge.
     * This method validates that the user has access to the challenge before creating the subscription.
     *
     * @param command The command containing the challengeId and userId for subscription.
     * @return An SseEmitter instance that the client can use to receive events.
     */
    SseEmitter subscribeToChallenge(SubscribeToChallengeCommand command);
}
