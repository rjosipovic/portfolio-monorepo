package com.playground.analytics_manager.inbound.messaging.consumers;

import com.playground.analytics_manager.inbound.messaging.events.UserLifecycleEvent;
import com.playground.analytics_manager.inbound.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersEventConsumer {

    private final UserService userService;

    @RabbitListener(queues = "#{userQueue.name}", ackMode = "AUTO")
    public void handleUserEvent(UserLifecycleEvent event) {
        log.info("Received user event: {}", event);
        userService.processUser(event);
    }
}
