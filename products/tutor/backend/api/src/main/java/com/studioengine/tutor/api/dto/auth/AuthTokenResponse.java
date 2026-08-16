package com.studioengine.tutor.api.dto.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = AuthTokenResponse.AuthTokenResponseBuilder.class)
public class AuthTokenResponse {

    String accessToken;
    long expiresIn;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AuthTokenResponseBuilder {}

}
