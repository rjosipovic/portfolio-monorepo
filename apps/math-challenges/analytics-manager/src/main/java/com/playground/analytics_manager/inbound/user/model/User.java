package com.playground.analytics_manager.inbound.user.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class User {

    private final String id;
    private final String alias;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public User(
            @JsonProperty("id") String id,
            @JsonProperty("alias") String alias
    ) {
        this.id = id;
        this.alias = alias;
    }
}
