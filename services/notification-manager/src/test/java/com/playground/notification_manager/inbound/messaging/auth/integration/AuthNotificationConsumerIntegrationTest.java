package com.playground.notification_manager.inbound.messaging.auth.integration;

import com.playground.notification_manager.inbound.messaging.auth.AuthNotification;
import com.playground.notification_manager.outbound.email.EmailMessage;
import com.playground.notification_manager.outbound.email.EmailSender;
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

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false"
})
@Import(AuthNotificationConsumerIntegrationTest.TestConfig.class)
class AuthNotificationConsumerIntegrationTest {

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
    private EmailSender emailSender; // This will be the mock bean

    @Test
    void whenAuthNotificationIsReceived_thenEmailIsSent() {
        // given
        var exchange = "notifications-exchange";
        var routingKey = "notifications.auth";
        var notification = AuthNotification.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .body("Test body")
                .build();

        // when
        rabbitTemplate.convertAndSend(exchange, routingKey, notification);

        // then
        var emailMessageCaptor = ArgumentCaptor.forClass(EmailMessage.class);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(emailSender).sendEmail(emailMessageCaptor.capture());
        });

        var capturedEmail = emailMessageCaptor.getValue();
        assertAll(
                () -> assertThat(capturedEmail.getTo()).isEqualTo(notification.getTo()),
                () -> assertThat(capturedEmail.getSubject()).isEqualTo(notification.getSubject()),
                () -> assertThat(capturedEmail.getBody()).isEqualTo(notification.getBody())
        );
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public EmailSender emailSender() {
            return mock(EmailSender.class);
        }
    }
}
