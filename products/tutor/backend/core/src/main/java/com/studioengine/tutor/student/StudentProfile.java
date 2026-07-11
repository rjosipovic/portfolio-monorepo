package com.studioengine.tutor.student;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudentProfile {

    UUID id;
    String name;
    String email;
    String phone;
    Metrics metrics;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Metrics {
        int totalLessonsCompleted;
        BigDecimal totalRevenue;
        LocalDate lastLessonDate;
    }
}
