package com.playground.user_manager.auth.messaging.producers.integration;

import com.playground.user_manager.auth.messaging.AuthNotification;
import com.playground.user_manager.auth.messaging.producers.AuthMessageProducer;
import com.playground.user_manager.messaging.callback.CallbackManager;
import com.playground.user_manager.user.dataaccess.UserRepository;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
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
@Import(AuthMessageProducerIntegrationTest.RabbitTestConfig.class)
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class, LiquibaseAutoConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthMessageProducerIntegrationTest {

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
    private AuthMessageProducer authMessageProducer;

    @Autowired
    private TestListener testListener;

    @Autowired
    private CallbackManager callbackManager;

    @Test
    void whenSendAuthCode_thenMessageIsConsumed() throws InterruptedException {
        // given
        var authNotification = AuthNotification.builder().to("test@email.com").subject("Test Subject").body("Your code is 123456").build();

        // when
        authMessageProducer.sendAuthCode(authNotification);

        // then
        var receivedNotification = testListener.getMessages().poll(5, TimeUnit.SECONDS);

        assertAll(
                () -> assertThat(receivedNotification).isNotNull(),
                () -> assertThat(receivedNotification.getTo()).isEqualTo(authNotification.getTo()),
                () -> assertThat(receivedNotification.getSubject()).isEqualTo(authNotification.getSubject()),
                () -> assertThat(receivedNotification.getBody()).isEqualTo(authNotification.getBody())
        );
        verify(callbackManager, times(1)).put(anyString(), any(Message.class));
    }

    @TestConfiguration
    static class RabbitTestConfig {

        private static final String TEST_QUEUE = "notifications.auth.test.queue";
        private static final String ROUTING_KEY = "notifications.auth";

        @Bean
        public Queue testQueue() {
            return new Queue(TEST_QUEUE, true);
        }

        @Bean
        public Binding testBinding(Queue testQueue, DirectExchange notificationsExchange) {
            return BindingBuilder.bind(testQueue).to(notificationsExchange).with(ROUTING_KEY);
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

        private final BlockingQueue<AuthNotification> messages = new LinkedBlockingQueue<>();

        @RabbitListener(queues = "notifications.auth.test.queue")
        public void receiveMessage(@Payload AuthNotification notification) {
            messages.add(notification);
        }
    }
}
