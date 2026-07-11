package com.studioengine.tutor.student;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NoteEntry {

    UUID id;
    String content;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
