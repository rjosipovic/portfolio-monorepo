package com.playground.analytics_manager.inbound.user;

import com.playground.analytics_manager.inbound.messaging.events.UserLifecycleEvent;
import com.playground.analytics_manager.inbound.user.lifecycle.UserLifecycleHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final List<UserLifecycleHandler> handlers;

    @Override
    public void processUser(UserLifecycleEvent event) {
        var user = event.getUser();
        var lifecycleType = event.getLifecycleType();
        handlers.stream()
                .filter(handler -> handler.supports() == lifecycleType)
                .findFirst()
                .ifPresent(handler -> handler.handle(user));
    }
}
