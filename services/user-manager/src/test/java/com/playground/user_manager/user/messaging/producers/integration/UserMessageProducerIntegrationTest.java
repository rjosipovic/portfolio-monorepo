package com.playground.user_manager.user.messaging.producers.integration;

import com.playground.user_manager.messaging.callback.CallbackManager;
import com.playground.user_manager.user.dataaccess.UserRepository;
import com.playground.user_manager.user.messaging.UserLifecycleEvent;
import com.playground.user_manager.user.messaging.producers.UserMessageProducer;
import com.playground.user_manager.user.model.User;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.annotation.DirtiesContext;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = { "app.auth.secret=some-dummy-secret-for-testing" })
@Import(UserMessageProducerIntegrationTest.RabbitTestConfig.class)
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class, LiquibaseAutoConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserMessageProducerIntegrationTest {

    @Container
    static final RabbitMQContainer rabbit = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3-management")
    );

    @Autowired
    private CallbackManager callbackManager;

    @DynamicPropertySource
    static void configureRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Autowired
    private UserMessageProducer userMessageProducer;

    @Autowired
    private TestListener testListener;

    @Test
    void whenSendUserCreatedMessage_thenMessageIsConsumed() throws InterruptedException {
        // given
        var user = User.builder().id(UUID.randomUUID().toString()).alias("test-alias").build();

        // when
        userMessageProducer.sendUserCreatedMessage(user);

        // then
        // Wait for the asynchronous listener to receive the message
        var receivedEvent = testListener.getMessages().poll(5, TimeUnit.SECONDS);

        assertAll(
            () -> assertThat(receivedEvent).isNotNull(),
            () -> assertThat(receivedEvent.getLifecycleType()).isEqualTo(com.playground.user_manager.user.messaging.LifecycleType.CREATED),
            () -> assertThat(receivedEvent.getUser().getId()).isEqualTo(user.getId()),
            () -> assertThat(receivedEvent.getUser().getAlias()).isEqualTo(user.getAlias())
        );
        verify(callbackManager, times(1)).put(anyString(), any(Message.class));
    }

    // --- Test Configuration for RabbitMQ Listener ---

    @TestConfiguration
    static class RabbitTestConfig {

        private static final String TEST_QUEUE = "user.created.test.queue";
        private static final String ROUTING_KEY = "user.created";

        @Bean
        public Queue testQueue() {
            return new Queue(TEST_QUEUE, true);
        }

        @Bean
        public Binding testBinding(Queue testQueue, TopicExchange userExchange) {
            return BindingBuilder.bind(testQueue).to(userExchange).with(ROUTING_KEY);
        }

        @Bean
        public TestListener testListener() {
            return new TestListener();
        }

        @Bean
        public CallbackManager callbackManager() {
            // Create a mock using Mockito and return it as the bean
            return mock(CallbackManager.class);
        }

        @Bean
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }

    @Getter
    public static class TestListener {

        private final BlockingQueue<UserLifecycleEvent> messages = new LinkedBlockingQueue<>();

        @RabbitListener(queues = "user.created.test.queue")
        public void receiveMessage(@Payload UserLifecycleEvent event) {
            messages.add(event);
        }

    }
}
