package com.playground.analytics_manager.inbound.user.lifecycle;

import com.playground.analytics_manager.inbound.user.model.User;
import com.playground.analytics_manager.inbound.user.model.UserLifecycleType;

public interface UserLifecycleHandler {

    void handle(User user);

    UserLifecycleType supports();
}
