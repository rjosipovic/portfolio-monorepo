package com.studioengine.tutor.api.dto.student;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = NoteResponse.NoteResponseBuilder.class)
public class NoteResponse {

    UUID id;
    String content;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;

    @JsonPOJOBuilder(withPrefix = "")
    public static class NoteResponseBuilder {}
}
