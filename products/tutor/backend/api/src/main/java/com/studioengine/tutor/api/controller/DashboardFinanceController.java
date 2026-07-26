package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.finance.ConfirmedPaymentResponse;
import com.studioengine.tutor.api.dto.finance.MonthlyRevenueResponse;
import com.studioengine.tutor.api.dto.finance.PendingPaymentResponse;
import com.studioengine.tutor.api.mapper.FinanceMapper;
import com.studioengine.tutor.finance.ConfirmBankTransferCommand;
import com.studioengine.tutor.finance.FinanceService;
import com.studioengine.tutor.finance.MonthlyRevenueQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard/finance")
@RequiredArgsConstructor
@Slf4j
public class DashboardFinanceController {

    private final FinanceService financeService;
    private final FinanceMapper financeMapper;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyRevenueResponse> getMonthlyRevenue(
            @RequestParam(name = "year", required = true) Integer year,
            @RequestParam(name = "month", required = true) Integer month
    ) {
        log.info("GET /dashboard/finance/monthly year={} month={}", year, month);
        var query = MonthlyRevenueQuery.builder()
                .year(year)
                .month(month)
                .build();
        var result = financeService.getMonthlyRevenue(query);
        var response = financeMapper.toMonthlyRevenueResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PendingPaymentResponse>> getPendingPayments() {
        log.info("GET /dashboard/finance/pending");
        var result = financeService.getPendingPayments();
        var response = financeMapper.toPendingPaymentResponseList(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-bank-transfer/{appointmentId}")
    public ResponseEntity<ConfirmedPaymentResponse> confirmBankTransfer(
            @PathVariable(name = "appointmentId") UUID appointmentId
    ) {
     log.info("POST /dashboard/finance/confirm-bank-transfer");
     var command = ConfirmBankTransferCommand.builder().appointmentId(appointmentId).build();
     var result = financeService.confirmBankTransfer(command);
     var response = financeMapper.toConfirmedPaymentResponse(result);
     return ResponseEntity.ok(response);
    }
}
