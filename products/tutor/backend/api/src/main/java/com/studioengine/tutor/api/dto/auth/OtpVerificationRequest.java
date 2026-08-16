package com.studioengine.tutor.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = OtpVerificationRequest.OtpVerificationRequestBuilder.class)
public class OtpVerificationRequest {

    @NotBlank @Email
    String email;
    @NotBlank @Positive
    String otp;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OtpVerificationRequestBuilder {}
}
