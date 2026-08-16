package com.studioengine.tutor.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = OtpRequest.OtpRequestBuilder.class)
public class OtpRequest {

    @NotBlank @Email
    String email;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OtpRequestBuilder {}
}
