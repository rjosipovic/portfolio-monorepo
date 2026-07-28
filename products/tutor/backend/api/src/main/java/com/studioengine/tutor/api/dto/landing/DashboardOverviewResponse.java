package com.studioengine.tutor.api.dto.landing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = DashboardOverviewResponse.DashboardOverviewResponseBuilder.class)
public class DashboardOverviewResponse {

    List<ActionItemResponse> awaitingClosure;
    List<ActionItemResponse> pendingPayments;
    List<ActionItemResponse> todayUpcoming;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonDeserialize(builder = ActionItemResponse.ActionItemResponseBuilder.class)
    public static class ActionItemResponse {

        UUID appointmentId;
        String studentName;
        String serviceCategory;
        LocalDate date;
        LocalTime startTime;
        BigDecimal amount;

        @JsonPOJOBuilder(withPrefix = "")
        public static class ActionItemResponseBuilder {}
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class DashboardOverviewResponseBuilder {}
}
