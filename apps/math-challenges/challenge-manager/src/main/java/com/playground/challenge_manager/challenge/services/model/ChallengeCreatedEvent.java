package com.playground.challenge_manager.challenge.services.model;

import lombok.Value;

import java.util.UUID;

/**
 * Published when a new challenge is created.
 * <p>
 * <strong>Intended Consumers:</strong>
 * <ul>
 *   <li>Background workers that calculate the answer and update the status to PENDING.</li>
 * </ul>
 * </p>
 */
@Value
public class ChallengeCreatedEvent {

    UUID challengeId;
}
