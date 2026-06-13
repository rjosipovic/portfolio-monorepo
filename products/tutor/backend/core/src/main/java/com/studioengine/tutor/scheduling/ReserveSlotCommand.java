package com.studioengine.tutor.scheduling;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReserveSlotCommand {

    UUID slotId;
    String rescheduleToken; // nullable, added later without changing interface
}
