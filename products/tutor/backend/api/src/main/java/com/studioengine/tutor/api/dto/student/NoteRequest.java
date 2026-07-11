package com.studioengine.tutor.api.dto.student;

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
@JsonDeserialize(builder = NoteRequest.NoteRequestBuilder.class)
public class NoteRequest {

    @NotBlank
    String content;

    @JsonPOJOBuilder(withPrefix = "")
    public static class NoteRequestBuilder {}
}
