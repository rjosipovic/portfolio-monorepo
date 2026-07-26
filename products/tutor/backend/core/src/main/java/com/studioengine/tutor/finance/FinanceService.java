package com.studioengine.tutor.finance;

import java.util.List;

public interface FinanceService {

    MonthlyRevenue getMonthlyRevenue(MonthlyRevenueQuery query);

    List<PendingPayment> getPendingPayments();

    ConfirmedPayment confirmBankTransfer(ConfirmBankTransferCommand command);
}
