package com.studioengine.tutor.finance;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MonthlyRevenue {

    int year;
    int month;
    BigDecimal totalRevenue;
    int billableHours;
    BigDecimal stripePayments;
    BigDecimal bankTransferPayments;
    BigDecimal cashPayments;
    int completedAppointments;
}
