package com.playground.analytics_manager.inbound.user;

import com.playground.analytics_manager.inbound.messaging.events.UserLifecycleEvent;

public interface UserService {

    void processUser(UserLifecycleEvent event);
}
