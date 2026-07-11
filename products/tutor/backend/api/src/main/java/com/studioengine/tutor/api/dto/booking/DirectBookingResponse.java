package com.studioengine.tutor.api.dto.booking;

import com.studioengine.tutor.api.dto.summary.AppointmentSummary;
import com.studioengine.tutor.api.dto.summary.ServiceCategorySummary;
import com.studioengine.tutor.api.dto.summary.StudentSummary;
import com.studioengine.tutor.api.dto.summary.TimeSlotSummary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = DirectBookingResponse.DirectBookingResponseBuilder.class)
public class DirectBookingResponse {

    AppointmentSummary appointment;
    TimeSlotSummary timeSlot;
    StudentSummary student;
    ServiceCategorySummary serviceCategory;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DirectBookingResponseBuilder {}
}
