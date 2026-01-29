package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.challenge.api.dto.ChallengeResponse;
import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeEntity;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ChallengeMapper challengeMapper; // Inject mapper

    public SseEmitter subscribe(ChallengeEntity challengeEntity) {
        var challengeId = challengeEntity.getId();
        var emitter = new SseEmitter(Long.MAX_VALUE); // No timeout

        emitter.onCompletion(() -> {
            log.info("SSE emitter completed for challenge {}", challengeId);
            this.emitters.remove(challengeId);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE emitter timed out for challenge {}", challengeId);
            emitter.complete();
            this.emitters.remove(challengeId);
        });
        emitter.onError(e -> {
            log.error("SSE emitter error for challenge {}: {}", challengeId, e.getMessage());
            this.emitters.remove(challengeId);
        });

        // Check current status of the challenge
        if (challengeEntity.getStatus() != ChallengeStatus.GENERATED) {
            // If already processed, send the current state immediately and complete
            try {
                var response = challengeMapper.toChallengeResponse(challengeEntity);
                emitter.send(SseEmitter.event().name("challenge-ready").data(response));
                log.info("Sent immediate 'challenge-ready' event for already processed challenge {}", challengeId);
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send immediate SSE event for challenge {}: {}", challengeId, e.getMessage());
                emitter.completeWithError(e);
            }
        } else {
            // If still GENERATED, store the emitter to wait for the event
            this.emitters.put(challengeId, emitter);
            log.info("New SSE subscriber for challenge {}", challengeId);
        }
        return emitter;
    }

    public void sendToClient(UUID challengeId, ChallengeResponse data) {
        var emitter = this.emitters.get(challengeId);
        if (Objects.nonNull(emitter)) {
            try {
                emitter.send(SseEmitter.event().name("challenge-ready").data(data));
                log.info("Sent 'challenge-ready' event to client for challenge {}", challengeId);
                emitter.complete(); // Close the connection after sending the data
            } catch (IOException e) {
                log.error("Failed to send SSE event for challenge {}: {}", challengeId, e.getMessage());
                emitter.completeWithError(e);
            }
        } else {
            log.warn("No active SSE emitter found for challenge {}", challengeId);
        }
    }
}
