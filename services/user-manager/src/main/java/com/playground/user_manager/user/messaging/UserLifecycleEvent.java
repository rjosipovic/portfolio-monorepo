package com.playground.user_manager.user.messaging;

import com.playground.user_manager.user.model.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserLifecycleEvent {

    User user;
    LifecycleType lifecycleType;
}
