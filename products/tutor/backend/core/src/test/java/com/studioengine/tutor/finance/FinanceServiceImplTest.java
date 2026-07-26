package com.studioengine.tutor.finance;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.PaymentRecord;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.PaymentMethod;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.PaymentRecordRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceImplTest {

    @Mock
    private PaymentRecordRepository paymentRecordRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private AppointmentStateMachine appointmentStateMachine;
    @Mock
    private TimeSlotStateMachine timeSlotStateMachine;

    @InjectMocks
    private FinanceServiceImpl financeService;

    @Nested
    class MonthlyRevenue {

        @Test
        void shouldReturnMonthlyRevenue() {
            // given
            var year = 2026;
            var month = 6;
            var query = MonthlyRevenueQuery.builder()
                    .year(year)
                    .month(month)
                    .build();
            var from = LocalDate.of(year, month, 1);
            var to = from.withDayOfMonth(from.lengthOfMonth());

            var mockedRecords = new MockedRecords();
            var paymentRecords = mockedRecords.paymentRecords;
            var appointments = mockedRecords.appointments;
            when(paymentRecordRepository.findByPaymentDateBetween(from ,to)).thenReturn(paymentRecords);
            when(appointmentRepository.findByStatesAndSlotDateBetween(Set.of(AppointmentState.COMPLETED), from, to)).thenReturn(appointments);

            // when
            var result = financeService.getMonthlyRevenue(query);

            // then
            verify(paymentRecordRepository).findByPaymentDateBetween(from, to);
            verify(appointmentRepository).findByStatesAndSlotDateBetween(Set.of(AppointmentState.COMPLETED), from, to);

            assertThat(result.getYear()).isEqualTo(year);
            assertThat(result.getMonth()).isEqualTo(month);
            assertThat(result.getTotalRevenue()).isEqualTo(mockedRecords.totalRevenue);
            assertThat(result.getStripePayments()).isEqualTo(mockedRecords.stripeRevenue);
            assertThat(result.getBankTransferPayments()).isEqualTo(mockedRecords.bankTransferRevenue);
            assertThat(result.getCashPayments()).isEqualTo(mockedRecords.cashRevenue);

            assertThat(result.getCompletedAppointments()).isEqualTo(appointments.size());
            assertThat(result.getBillableHours()).isEqualTo(appointments.size());
        }

        @Test
        void shouldReturnEmptyMonthlyRevenue() {
            // given
            var year = 2026;
            var month = 6;
            var query = MonthlyRevenueQuery.builder()
                    .year(year)
                    .month(month)
                    .build();
            var from = LocalDate.of(year, month, 1);
            var to = from.withDayOfMonth(from.lengthOfMonth());

            when(paymentRecordRepository.findByPaymentDateBetween(from ,to)).thenReturn(List.of());
            when(appointmentRepository.findByStatesAndSlotDateBetween(Set.of(AppointmentState.COMPLETED), from, to)).thenReturn(List.of());

            // when
            var result = financeService.getMonthlyRevenue(query);

            // then
            verify(paymentRecordRepository).findByPaymentDateBetween(from, to);
            verify(appointmentRepository).findByStatesAndSlotDateBetween(Set.of(AppointmentState.COMPLETED), from, to);

            assertThat(result.getYear()).isEqualTo(year);
            assertThat(result.getMonth()).isEqualTo(month);
            assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getStripePayments()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getBankTransferPayments()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getCashPayments()).isEqualTo(BigDecimal.ZERO);

            assertThat(result.getCompletedAppointments()).isEqualTo(0);
            assertThat(result.getBillableHours()).isEqualTo(0);
        }
    }

    @Nested
    class PendingPayments {

        @Test
        void shouldReturnPendingPayments() {
            // given
            var appointmentId = UUID.randomUUID();
            var studentName = "Marko Markić";
            var amount = BigDecimal.valueOf(25);
            var createdAt = OffsetDateTime.now().minusDays(1);
            var pendingPayment = mock(Appointment.class);
            var student = mock(Student.class);
            when(student.getName()).thenReturn(studentName);
            when(pendingPayment.getId()).thenReturn(appointmentId);
            when(pendingPayment.getStudent()).thenReturn(student);
            when(pendingPayment.getFinalPrice()).thenReturn(amount);
            when(pendingPayment.getCreatedAt()).thenReturn(createdAt);
            when(appointmentRepository.findByStateIn(Set.of(AppointmentState.PENDING_PAYMENT))).thenReturn(List.of(pendingPayment));

            // when
            var result = financeService.getPendingPayments();

            // then
            verify(appointmentRepository).findByStateIn(Set.of(AppointmentState.PENDING_PAYMENT));

            assertThat(result).hasSize(1);
        }

        @Test
        void shouldReturnEmptyPendingPayments() {
            // given
            when(appointmentRepository.findByStateIn(Set.of(AppointmentState.PENDING_PAYMENT))).thenReturn(List.of());

            // when
            var result = financeService.getPendingPayments();

            // then
            verify(appointmentRepository).findByStateIn(Set.of(AppointmentState.PENDING_PAYMENT));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ConfirmBanTransfer {

        @Test
        void shouldConfirmBankTransfer() {
            // given
            var appointmentId = UUID.randomUUID();
            var finalPrice = BigDecimal.TEN;
            var command = ConfirmBankTransferCommand.builder().appointmentId(appointmentId).build();
            var timeSlot = mock(TimeSlot.class);
            var appointment = mock(Appointment.class);

            when(appointment.getId()).thenReturn(appointmentId);
            when(appointment.getTimeSlot()).thenReturn(timeSlot);
            when(appointment.getFinalPrice()).thenReturn(finalPrice);
            when(appointment.getState()).thenReturn(AppointmentState.CONFIRMED);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

            // when
            var result = financeService.confirmBankTransfer(command);

            // then
            verify(appointmentRepository).findById(appointmentId);
            verify(appointmentStateMachine).transition(appointment, AppointmentState.CONFIRMED, "TUTOR_BANK_CONFIRM");
            verify(timeSlotStateMachine).transition(timeSlot, TimeSlotState.BOOKED, "TUTOR_BANK_CONFIRM");
            verify(appointmentRepository).save(appointment);
            verify(timeSlotRepository).save(timeSlot);

            var captor = ArgumentCaptor.forClass(PaymentRecord.class);
            verify(paymentRecordRepository).save(captor.capture());
            var paymentRecordToSave = captor.getValue();
            assertThat(paymentRecordToSave.getAppointment()).isEqualTo(appointment);
            assertThat(paymentRecordToSave.getAmount()).isEqualTo(appointment.getFinalPrice());
            assertThat(paymentRecordToSave.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
            assertThat(paymentRecordToSave.getStripePaymentId()).isNull();

            assertThat(result.getAppointmentId()).isEqualTo(appointment.getId());
            assertThat(result.getState()).isEqualTo(AppointmentState.CONFIRMED.toString());
        }
    }

    @Test
    void shouldNotConfirmBankTransferWhenAppointmentNotExists() {
        // given
        var appointmentId = UUID.randomUUID();
        var command = ConfirmBankTransferCommand.builder().appointmentId(appointmentId).build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> financeService.confirmBankTransfer(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentStateMachine, never()).transition(any(), any(), any());
    }

    @Test
    void shouldNotConfirmBankTransferWhenAppointmentInTerminalState() {
        // given
        var appointmentId = UUID.randomUUID();
        var command = ConfirmBankTransferCommand.builder().appointmentId(appointmentId).build();
        var appointment = mock(Appointment.class);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        doThrow(new InvalidStateTransitionException("Appointment is in terminal state"))
                .when(appointmentStateMachine)
                .transition(appointment, AppointmentState.CONFIRMED, "TUTOR_BANK_CONFIRM");

        // when
        assertThatThrownBy(() -> financeService.confirmBankTransfer(command)).isInstanceOf(InvalidStateTransitionException.class);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentStateMachine).transition(appointment, AppointmentState.CONFIRMED, "TUTOR_BANK_CONFIRM");
        verify(timeSlotStateMachine, never()).transition(any(), any(), any());
    }

    static class MockedRecords {

        private final List<Appointment> appointments;

        private final BigDecimal stripeRevenue;
        private final BigDecimal bankTransferRevenue;
        private final BigDecimal cashRevenue;
        private final BigDecimal totalRevenue;

        private final List<PaymentRecord> paymentRecords;

        public MockedRecords() {
            var stripe1 = mockRecord(PaymentMethod.STRIPE, BigDecimal.valueOf(25));
            var stripe2 = mockRecord(PaymentMethod.STRIPE, BigDecimal.valueOf(35));
            var stripe3 = mockRecord(PaymentMethod.STRIPE, BigDecimal.valueOf(30));
            var bt1 = mockRecord(PaymentMethod.BANK_TRANSFER, BigDecimal.valueOf(22));
            var bt2 = mockRecord(PaymentMethod.BANK_TRANSFER, BigDecimal.valueOf(27));
            var bt3 = mockRecord(PaymentMethod.BANK_TRANSFER, BigDecimal.valueOf(35));
            var c1 = mockRecord(PaymentMethod.CASH, BigDecimal.valueOf(25));
            var c2 = mockRecord(PaymentMethod.CASH, BigDecimal.valueOf(22));
            var c3 = mockRecord(PaymentMethod.CASH, BigDecimal.valueOf(35));
            paymentRecords = Stream.of(stripe1, stripe2, stripe3, bt1, bt2, bt3, c1, c2, c3).toList();

            var total = BigDecimal.ZERO;
            var stripe = BigDecimal.ZERO;
            var bankTransfer = BigDecimal.ZERO;
            var cash = BigDecimal.ZERO;

            for (var p : paymentRecords) {
                total = total.add(p.getAmount());

                switch (p.getPaymentMethod()) {
                    case STRIPE -> stripe = stripe.add(p.getAmount());
                    case BANK_TRANSFER -> bankTransfer = bankTransfer.add(p.getAmount());
                    case  CASH -> cash = cash.add(p.getAmount());
                }
            }
            stripeRevenue = stripe;
            bankTransferRevenue = bankTransfer;
            cashRevenue = cash;
            totalRevenue = total;

            appointments = Stream.generate(() -> mock(Appointment.class))
                    .limit(10)
                    .toList();
        }

        private PaymentRecord mockRecord(PaymentMethod method, BigDecimal amount) {
            var record = mock(PaymentRecord.class);
            when(record.getPaymentMethod()).thenReturn(method);
            when(record.getAmount()).thenReturn(amount);
            return record;
        }
    }
}