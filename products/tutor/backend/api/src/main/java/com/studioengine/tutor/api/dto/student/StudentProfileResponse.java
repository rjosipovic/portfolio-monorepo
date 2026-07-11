package com.studioengine.tutor.api.dto.student;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = StudentProfileResponse.StudentProfileResponseBuilder.class)
public class StudentProfileResponse {

    UUID id;
    String name;
    String email;
    String phone;
    MetricsResponse metrics;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonDeserialize(builder = MetricsResponse.MetricsResponseBuilder.class)
    public static class MetricsResponse {
        int totalLessonsCompleted;
        BigDecimal totalRevenue;
        LocalDate lastLessonDate;

        @JsonPOJOBuilder(withPrefix = "")
        public static class MeticsResponseBuilder {}
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class StudentProfileResponseBuilder {}
}
