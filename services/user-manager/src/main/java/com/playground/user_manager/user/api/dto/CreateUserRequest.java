package com.playground.user_manager.user.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = CreateUserRequest.CreateUserRequestBuilder.class)
public class CreateUserRequest {

    @NotBlank
    String alias;
    @NotBlank
    @Email
    String email;
    @Past
    LocalDate birthdate;
    @Pattern(regexp = "male|female")
    String gender;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CreateUserRequestBuilder {}
}
