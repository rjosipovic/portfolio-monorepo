package com.studioengine.tutor.finance;

import com.studioengine.tutor.dataaccess.entities.PaymentRecord;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.PaymentMethod;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.PaymentRecordRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentStateMachine appointmentStateMachine;
    private final TimeSlotStateMachine timeSlotStateMachine;


    @Override
    public MonthlyRevenue getMonthlyRevenue(MonthlyRevenueQuery query) {
        var year = query.getYear();
        var month = query.getMonth();

        var from = LocalDate.of(year, month, 1);
        var to = from.withDayOfMonth(from.lengthOfMonth());

        var paymentRecords = paymentRecordRepository.findByPaymentDateBetween(from, to);

        var revenue = calculateRevenue(paymentRecords);
        var completedAppointments = countCompleteAppointments(from, to);

        return MonthlyRevenue.builder()
                .year(year)
                .month(month)
                .totalRevenue(revenue.total)
                .billableHours(completedAppointments)
                .stripePayments(revenue.stripe)
                .bankTransferPayments(revenue.bankTransfer)
                .cashPayments(revenue.cash)
                .completedAppointments(completedAppointments)
                .build();
    }

    @Override
    public List<PendingPayment> getPendingPayments() {
        var pendingPaymentAppointments = appointmentRepository.findByStateIn(Set.of(AppointmentState.PENDING_PAYMENT));
        return pendingPaymentAppointments.stream()
                .map(a -> PendingPayment.builder()
                        .appointmentId(a.getId())
                        .studentName(a.getStudent().getName())
                        .amount(a.getFinalPrice())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ConfirmedPayment confirmBankTransfer(ConfirmBankTransferCommand command) {
        var appointmentId = command.getAppointmentId();
        var appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: %s".formatted(appointmentId)));

        appointmentStateMachine.transition(appointment, AppointmentState.CONFIRMED, "TUTOR_BANK_CONFIRM");
        timeSlotStateMachine.transition(appointment.getTimeSlot(), TimeSlotState.BOOKED, "TUTOR_BANK_CONFIRM");

        appointmentRepository.save(appointment);
        timeSlotRepository.save(appointment.getTimeSlot());

        var paymentRecord = PaymentRecord.create(
                appointment,
                appointment.getFinalPrice(),
                PaymentMethod.BANK_TRANSFER,
                LocalDate.now(),
                null);
        paymentRecordRepository.save(paymentRecord);

        return ConfirmedPayment.builder()
                .appointmentId(appointment.getId())
                .state(appointment.getState().name())
                .confirmedAt(appointment.getStateChangedAt())
                .build();
    }

    private RevenueBreakdown calculateRevenue(List<PaymentRecord> paymentRecords) {
        var byPaymentMethod = paymentRecords.stream().collect(
                Collectors.groupingBy(
                        PaymentRecord::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, PaymentRecord::getAmount, BigDecimal::add))
        );
        var stripe = byPaymentMethod.getOrDefault(PaymentMethod.STRIPE, BigDecimal.ZERO);
        var bankTransfer = byPaymentMethod.getOrDefault(PaymentMethod.BANK_TRANSFER, BigDecimal.ZERO);
        var cash = byPaymentMethod.getOrDefault(PaymentMethod.CASH, BigDecimal.ZERO)
                .add(byPaymentMethod.getOrDefault(PaymentMethod.OTHER, BigDecimal.ZERO));
        var total = Stream.of(stripe, bankTransfer, cash).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RevenueBreakdown(total, stripe, bankTransfer, cash);
    }

    private int countCompleteAppointments(LocalDate from, LocalDate to) {
        return appointmentRepository.findByStatesAndSlotDateBetween(
            Set.of(AppointmentState.COMPLETED), from, to).size();
    }

    private record RevenueBreakdown(
            BigDecimal total,
            BigDecimal stripe,
            BigDecimal bankTransfer,
            BigDecimal cash
    ) {}
}
