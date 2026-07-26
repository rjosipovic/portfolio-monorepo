package com.studioengine.tutor.api.dto.finance;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = MonthlyRevenueResponse.MonthlyRevenueResponseBuilder.class)
public class MonthlyRevenueResponse {

    int year;
    int month;
    BigDecimal totalRevenue;
    int billableHours;
    BigDecimal stripePayments;
    BigDecimal bankTransferPayments;
    BigDecimal cashPayments;
    int completedAppointments;

    @JsonPOJOBuilder(withPrefix = "")
    public static class MonthlyRevenueResponseBuilder {}
}
