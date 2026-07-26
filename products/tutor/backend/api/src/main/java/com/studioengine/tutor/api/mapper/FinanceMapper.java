package com.studioengine.tutor.api.mapper;

import com.studioengine.tutor.api.dto.finance.ConfirmedPaymentResponse;
import com.studioengine.tutor.api.dto.finance.MonthlyRevenueResponse;
import com.studioengine.tutor.api.dto.finance.PendingPaymentResponse;
import com.studioengine.tutor.finance.ConfirmedPayment;
import com.studioengine.tutor.finance.MonthlyRevenue;
import com.studioengine.tutor.finance.PendingPayment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FinanceMapper {

    MonthlyRevenueResponse toMonthlyRevenueResponse(MonthlyRevenue monthlyRevenue);

    List<PendingPaymentResponse> toPendingPaymentResponseList(List<PendingPayment> pendingPayments);

    ConfirmedPaymentResponse toConfirmedPaymentResponse(ConfirmedPayment confirmedPayment);
}
