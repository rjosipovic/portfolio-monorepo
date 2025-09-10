package com.playground.user_manager.auth.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = AuthCodeGenerationRequest.AuthCodeGenerationRequestBuilder.class)
public class AuthCodeGenerationRequest {

    @NotNull @Email
    String email;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AuthCodeGenerationRequestBuilder {
    }
}
