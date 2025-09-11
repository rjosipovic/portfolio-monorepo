package com.playground.analytics_manager.dataaccess.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
@Disabled
class ChallengeRepositoryTest {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    void testGetChallengeByUserAttempt_UserId() {
        var userId = UUID.fromString("f92b8317-e734-4175-af75-b579e3e79975");
        var challengeEntities = challengeRepository.findByUserAttempt_UserId(userId);
        challengeEntities.forEach(System.out::println);
    }
}