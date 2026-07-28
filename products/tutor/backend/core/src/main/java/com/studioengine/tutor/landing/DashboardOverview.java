package com.studioengine.tutor.landing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardOverview {

    List<ActionItem> awaitingClosure;
    List<ActionItem> pendingPayments;
    List<ActionItem> todayUpcoming;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ActionItem {

        UUID appointmentId;
        String studentName;
        String serviceCategory;
        LocalDate date;
        LocalTime startTime;
        BigDecimal amount;
    }
}
