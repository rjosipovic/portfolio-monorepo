package com.playground.analytics_manager.inbound.messaging.consumers.integration;

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration;
import com.playground.analytics_manager.inbound.challenge.ChallengeService;
import com.playground.analytics_manager.inbound.messaging.events.ChallengeSolvedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "app.auth.secret=some-dummy-secret-for-testing",
        "spring.cloud.discovery.enabled=false"
})
@Import(ChallengeEventConsumerIntegrationTest.TestConfig.class)
@EnableAutoConfiguration(exclude = { MigrationsAutoConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ChallengeEventConsumerIntegrationTest {

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
    private ChallengeService challengeService; // This will be the mock bean

    @Test
    void whenChallengeEventIsReceived_thenChallengeServiceIsCalled() {
        // given
        var exchange = "challenge-exchange";
        var routingKey = "challenge-solved.correct"; // A key that matches the 'challenge-solved.*' binding
        var event = ChallengeSolvedEvent.builder()
                .userId(UUID.randomUUID().toString())
                .challengeAttemptId(UUID.randomUUID().toString())
                .firstNumber(10)
                .secondNumber(20)
                .resultAttempt(200)
                .correct(true)
                .game("addition")
                .difficulty("easy")
                .build();

        // when
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        // then
        var eventCaptor = ArgumentCaptor.forClass(ChallengeSolvedEvent.class);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(challengeService).process(eventCaptor.capture());
        });

        ChallengeSolvedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getChallengeAttemptId()).isEqualTo(event.getChallengeAttemptId());
        assertThat(capturedEvent.getUserId()).isEqualTo(event.getUserId());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ChallengeService challengeService() {
            return mock(ChallengeService.class);
        }
    }
}
