package com.studioengine.tutor.errors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = ErrorResponse.ErrorResponseBuilder.class)
public class ErrorResponse {

    String message;
    String code;
    String reason;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ErrorResponseBuilder {}
}
