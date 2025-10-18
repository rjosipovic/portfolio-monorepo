package com.playground.analytics_manager.inbound.user.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = User.UserBuilder.class)
public class User {

    String id;
    String alias;

    @JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
    public static class UserBuilder {}

}
