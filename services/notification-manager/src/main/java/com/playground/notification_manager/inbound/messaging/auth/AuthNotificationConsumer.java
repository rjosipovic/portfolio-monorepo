package com.playground.notification_manager.inbound.messaging.auth;

import com.playground.notification_manager.mappers.NotificationMapper;
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
    private final NotificationMapper notificationMapper;

    @RabbitListener(queues = "#{notificationQueue.name}", ackMode = "AUTO")
    public void consume(AuthNotification notification) {
        log.info("Accepting auth notification {}", notification);
        var emailMessage = notificationMapper.toEmailMessage(notification, emailConfig);
        applicationEventPublisher.publishEvent(emailMessage);
    }
}
