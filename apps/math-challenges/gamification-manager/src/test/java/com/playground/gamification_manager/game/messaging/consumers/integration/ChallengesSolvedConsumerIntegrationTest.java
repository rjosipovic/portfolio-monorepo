package com.playground.gamification_manager.game.messaging.consumers.integration;

import com.playground.gamification_manager.game.messaging.events.ChallengeSolvedEvent;
import com.playground.gamification_manager.game.service.interfaces.GameService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = { "app.auth.secret=some-dummy-secret-for-testing" })
@Import(ChallengesSolvedConsumerIntegrationTest.TestConfig.class)
class ChallengesSolvedConsumerIntegrationTest {

    @Container
    static final RabbitMQContainer rabbit = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3-management")
    );

    @DynamicPropertySource
    static void configureRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GameService gameService; // This will be the mock bean

    @Test
    void whenChallengeSolvedEventIsReceived_thenGameServiceIsCalled() {
        // given
        var exchange = "challenge-exchange";
        var routingKey = "challenge-solved.correct";
        var event = ChallengeSolvedEvent.builder()
                .challengeAttemptId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .alias("test-user")
                .firstNumber(10)
                .secondNumber(20)
                .correct(true)
                .game("multiplication")
                .build();

        // when
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        // then
        // Use Awaitility to wait for the asynchronous listener to process the message
        // and call the mocked GameService.
        var eventCaptor = ArgumentCaptor.forClass(ChallengeSolvedEvent.class);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(gameService).process(eventCaptor.capture());
        });

        var capturedEvent = eventCaptor.getValue();
        assertAll(
                () -> assertThat(capturedEvent.getChallengeAttemptId()).isEqualTo(event.getChallengeAttemptId()),
                () -> assertThat(capturedEvent.getUserId()).isEqualTo(event.getUserId()),
                () -> assertThat(capturedEvent.getCorrect()).isTrue()
        );
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public GameService gameService() {
            return mock(GameService.class);
        }
    }
}
