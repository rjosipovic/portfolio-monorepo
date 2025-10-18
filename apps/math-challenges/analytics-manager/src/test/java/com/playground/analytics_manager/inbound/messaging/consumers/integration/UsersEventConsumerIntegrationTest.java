package com.playground.analytics_manager.inbound.messaging.consumers.integration;


import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration;
import com.playground.analytics_manager.inbound.messaging.events.UserLifecycleEvent;
import com.playground.analytics_manager.inbound.user.UserService;
import com.playground.analytics_manager.inbound.user.model.User;
import com.playground.analytics_manager.inbound.user.model.UserLifecycleType;
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
@TestPropertySource(properties = { "app.auth.secret=some-dummy-secret-for-testing" })
@Import(UsersEventConsumerIntegrationTest.TestConfig.class)
@EnableAutoConfiguration(exclude = { MigrationsAutoConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UsersEventConsumerIntegrationTest {

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
    private UserService userService; // This will be the mock bean

    @Test
    void whenUserCreatedEventIsReceived_thenUserServiceIsCalled() {
        // given
        var exchange = "user-exchange";
        var routingKey = "user.created"; // A key that matches the 'user.*' binding
        var user = User.builder().id(UUID.randomUUID().toString()).alias("test-alias").build();
        var event = new UserLifecycleEvent(user, UserLifecycleType.CREATED);

        // when
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        // then
        var eventCaptor = ArgumentCaptor.forClass(UserLifecycleEvent.class);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(userService).processUser(eventCaptor.capture());
        });

        UserLifecycleEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getLifecycleType()).isEqualTo(UserLifecycleType.CREATED);
        assertThat(capturedEvent.getUser().getId()).isEqualTo(user.getId());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }
}
