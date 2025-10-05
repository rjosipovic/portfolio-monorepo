package com.playground.challenge_manager.challenge.messaging.producers.integration;

import com.playground.challenge_manager.challenge.messaging.events.ChallengeSolvedEvent;
import com.playground.challenge_manager.challenge.messaging.producers.ChallengeSolvedProducer;
import com.playground.challenge_manager.messaging.callback.CallbackManager;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = { "app.auth.secret=some-dummy-secret-for-testing" })
@Import(ChallengeSolvedProducerIntegrationTest.RabbitTestConfig.class)
class ChallengeSolvedProducerIntegrationTest {

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
    private ChallengeSolvedProducer challengeSolvedProducer;

    @Autowired
    private CorrectAnswerListener correctAnswerListener;

    @Autowired
    private IncorrectAnswerListener incorrectAnswerListener;

    @Autowired
    private AllAnswersListener allAnswersListener;

    @Autowired
    private CallbackManager callbackManager;

    @Test
    void whenPublishCorrectAnswer_thenMessageIsConsumedByCorrectListener() throws InterruptedException {
        // given
        var event = ChallengeSolvedEvent.builder()
                .challengeAttemptId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .firstNumber(12)
                .secondNumber(15)
                .resultAttempt(27)
                .game("addition")
                .correct(true)
                .build();

        // when
        challengeSolvedProducer.publishChallengeSolvedMessage(event);

        // then
        var receivedCorrect = correctAnswerListener.getMessages().poll(5, TimeUnit.SECONDS);
        var receivedIncorrect = incorrectAnswerListener.getMessages().poll(100, TimeUnit.MILLISECONDS);
        var receivedAll = allAnswersListener.getMessages().poll(5, TimeUnit.SECONDS);

        assertThat(receivedCorrect).isNotNull();
        assertThat(receivedCorrect.getChallengeAttemptId()).isEqualTo(event.getChallengeAttemptId());
        assertThat(receivedIncorrect).isNull();
        assertThat(receivedAll).isNotNull();
        assertThat(receivedAll.getChallengeAttemptId()).isEqualTo(event.getChallengeAttemptId());
    }

    @Test
    void whenPublishIncorrectAnswer_thenMessageIsConsumedByIncorrectListener() throws InterruptedException {
        // given
        var event = ChallengeSolvedEvent.builder()
                .challengeAttemptId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .firstNumber(12)
                .secondNumber(15)
                .resultAttempt(227)
                .game("addition")
                .correct(false)
                .build();

        // when
        challengeSolvedProducer.publishChallengeSolvedMessage(event);

        // then
        var receivedIncorrect = incorrectAnswerListener.getMessages().poll(5, TimeUnit.SECONDS);
        var receivedCorrect = correctAnswerListener.getMessages().poll(100, TimeUnit.MILLISECONDS);
        var receivedAll = allAnswersListener.getMessages().poll(5, TimeUnit.SECONDS);

        assertThat(receivedIncorrect).isNotNull();
        assertThat(receivedIncorrect.getChallengeAttemptId()).isEqualTo(event.getChallengeAttemptId());
        assertThat(receivedCorrect).isNull();
        assertThat(receivedAll).isNotNull();
        assertThat(receivedAll.getChallengeAttemptId()).isEqualTo(event.getChallengeAttemptId());
    }

    @TestConfiguration
    static class RabbitTestConfig {

        // --- Configuration for the CORRECT answer listener ---
        @Bean
        public Queue correctTestQueue() {
            return new Queue("challenge.solved.correct.test.queue", true);
        }

        @Bean
        public Binding correctTestBinding(Queue correctTestQueue, TopicExchange challengeExchange) {
            return BindingBuilder.bind(correctTestQueue).to(challengeExchange).with("challenge-solved.correct");
        }

        @Bean
        public CorrectAnswerListener correctAnswerListener() {
            return new CorrectAnswerListener();
        }

        // --- Configuration for the INCORRECT answer listener ---
        @Bean
        public Queue incorrectTestQueue() {
            return new Queue("challenge.solved.incorrect.test.queue", true);
        }

        @Bean
        public Binding incorrectTestBinding(Queue incorrectTestQueue, TopicExchange challengeExchange) {
            return BindingBuilder.bind(incorrectTestQueue).to(challengeExchange).with("challenge-solved.incorrect");
        }

        @Bean
        public IncorrectAnswerListener incorrectAnswerListener() {
            return new IncorrectAnswerListener();
        }

        // --- Configuration for the CATCH-ALL answer listener ---
        @Bean
        public Queue allTestQueue() {
            return new Queue("challenge.solved.all.test.queue", true);
        }

        @Bean
        public Binding allTestBinding(Queue allTestQueue, TopicExchange challengeExchange) {
            // This uses a wildcard to catch all messages for challenge.solved.*
            return BindingBuilder.bind(allTestQueue).to(challengeExchange).with("challenge-solved.*");
        }

        @Bean
        public AllAnswersListener allAnswersListener() {
            return new AllAnswersListener();
        }

        @Bean
        public CallbackManager callbackManager() {
            return mock(CallbackManager.class);
        }
    }

    @Getter
    public static class CorrectAnswerListener {
        private final BlockingQueue<ChallengeSolvedEvent> messages = new LinkedBlockingQueue<>();

        @RabbitListener(queues = "challenge.solved.correct.test.queue")
        public void receiveMessage(@Payload ChallengeSolvedEvent event) {
            messages.add(event);
        }
    }

    @Getter
    public static class IncorrectAnswerListener {
        private final BlockingQueue<ChallengeSolvedEvent> messages = new LinkedBlockingQueue<>();

        @RabbitListener(queues = "challenge.solved.incorrect.test.queue")
        public void receiveMessage(@Payload ChallengeSolvedEvent event) {
            messages.add(event);
        }
    }

    @Getter
    public static class AllAnswersListener {
        private final BlockingQueue<ChallengeSolvedEvent> messages = new LinkedBlockingQueue<>();

        @RabbitListener(queues = "challenge.solved.all.test.queue")
        public void receiveMessage(@Payload ChallengeSolvedEvent event) {
            messages.add(event);
        }
    }
}
