package com.playground.notification_manager.inbound.messaging.auth;

import com.playground.notification_manager.outbound.email.EmailMessage;
import com.playground.notification_manager.outbound.email.config.EmailConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthNotificationConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final EmailConfig emailConfig;

    @RabbitListener(queues = "#{notificationQueue.name}", ackMode = "AUTO")
    public void consume(AuthNotification notification) {
        log.info("Accepting auth notification {}", notification);
        var from = emailConfig.getDefaultFrom();
        var to = notification.getTo();
        var subject = notification.getSubject();
        var body = notification.getBody();
        var emailMessage = EmailMessage.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .body(body)
                .build();
        applicationEventPublisher.publishEvent(emailMessage);
    }
}
