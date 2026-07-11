package com.studioengine.tutor.student;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateNoteCommand {

    UUID studentId;
    UUID noteId;
    String content;
}
