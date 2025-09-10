package com.playground.user_manager.auth.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = AuthCodeVerificationRequest.AuthCodeVerificationRequestBuilder.class)
public class AuthCodeVerificationRequest {

    @NotNull @Email
    String email;

    @NotBlank
    String code;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AuthCodeVerificationRequestBuilder {}
}
