package com.playground.analytics_manager.inbound.messaging.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.playground.analytics_manager.inbound.user.model.User;
import com.playground.analytics_manager.inbound.user.model.UserLifecycleType;
import lombok.Getter;

@Getter
public class UserLifecycleEvent {

    private final User user;
    private final UserLifecycleType lifecycleType;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public UserLifecycleEvent(
            @JsonProperty("user") User user,
            @JsonProperty("lifecycleType") UserLifecycleType lifecycleType
    ) {
        this.user = user;
        this.lifecycleType = lifecycleType;
    }
}
