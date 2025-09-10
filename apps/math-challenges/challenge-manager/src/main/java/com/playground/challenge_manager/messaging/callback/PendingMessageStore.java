package com.playground.challenge_manager.messaging.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PendingMessageStore {

    private static final String PENDING_MESSAGE_STORE_KEY = "pending_messages";
    private static final Duration TTL = Duration.ofHours(6);

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String correlationId, Object message) {
        var key = PENDING_MESSAGE_STORE_KEY + ":" + correlationId;
        redisTemplate.opsForValue().set(key, message, TTL);
    }

    public Object get(String correlationId) {
        var key = PENDING_MESSAGE_STORE_KEY + ":" + correlationId;
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String correlationId) {
        var key = PENDING_MESSAGE_STORE_KEY + ":" + correlationId;
        redisTemplate.delete(key);
    }
}
