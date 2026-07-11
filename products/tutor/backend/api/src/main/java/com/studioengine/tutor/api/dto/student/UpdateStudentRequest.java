package com.studioengine.tutor.api.dto.student;

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
@JsonDeserialize(builder = UpdateStudentRequest.UpdateStudentRequestBuilder.class)
public class UpdateStudentRequest {

    @NotBlank
    String name;

    @NotBlank
    @Email
    String email;

    @NotBlank
    String phone;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UpdateStudentRequestBuilder {}
}
