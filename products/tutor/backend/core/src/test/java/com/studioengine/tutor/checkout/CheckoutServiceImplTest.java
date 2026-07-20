package com.studioengine.tutor.checkout;

import com.studioengine.tutor.benefit.BenefitApplication;
import com.studioengine.tutor.benefit.BenefitService;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.BenefitType;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.ServiceCategoryRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.errors.exceptions.InvalidReservationException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.payment.PaymentCommand;
import com.studioengine.tutor.payment.PaymentInitiation;
import com.studioengine.tutor.payment.PaymentService;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TimeSlotStateMachine timeSlotStateMachine;
    @Mock
    private AppointmentStateMachine appointmentStateMachine;
    @Mock
    private PaymentService paymentService;
    @Mock
    private BenefitService benefitService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    // --- Happy path: Stripe ---
    @Test
    void shouldCheckoutWithStripePayment() {
        // given
        var slotId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var studentEmail = "ana@test.com";
        var sessionNotes = "Help with integrals";
        var categoryPrice = new BigDecimal(30);

        var slot = mock(TimeSlot.class);
        var category = mock(ServiceCategory.class);
        var student = mock(Student.class);
        var command = mock(CheckoutCommand.class);
        var benefitApplication = mock(BenefitApplication.class);
        var appointment = mock(Appointment.class);
        var paymentInitiation = mock(PaymentInitiation.class);

        when(command.getReservedSlotId()).thenReturn(slotId);
        when(command.getServiceCategoryId()).thenReturn(categoryId);
        when(command.getSessionNotes()).thenReturn(sessionNotes);
        when(command.getPaymentMethod()).thenReturn(PaymentMethodChoice.STRIPE);
        when(command.getGuestEmail()).thenReturn(studentEmail);
        when(slot.getState()).thenReturn(TimeSlotState.RESERVED);
        when(category.getPrice()).thenReturn(categoryPrice);
        when(benefitApplication.isApplied()).thenReturn(false);
        when(benefitApplication.getFinalPrice()).thenReturn(categoryPrice);

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(serviceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(studentRepository.findByEmail(studentEmail)).thenReturn(Optional.of(student));
        when(benefitService.apply(student, categoryPrice)).thenReturn(benefitApplication);
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(paymentService.initPayment(any())).thenReturn(paymentInitiation);
        when(appointmentRepository.findById(any())).thenReturn(Optional.of(appointment));

        // when
        checkoutService.checkout(command);

        // then
        verify(timeSlotRepository).findByIdForUpdate(slotId);
        verify(serviceCategoryRepository).findById(categoryId);
        verify(studentRepository).findByEmail(studentEmail);
        verify(benefitService).apply(student, categoryPrice);
        verify(appointmentRepository).save(any(Appointment.class));

        var captor = ArgumentCaptor.forClass(PaymentCommand.class);
        verify(paymentService).initPayment(captor.capture());

        var paymentCmd = captor.getValue();
        assertThat(paymentCmd.getPaymentMethod()).isEqualTo(PaymentMethodChoice.STRIPE);
        assertThat(paymentCmd.getAppointment()).isEqualTo(appointment);

        verify(appointmentRepository).findById(any());
        verify(benefitService).consume(benefitApplication, appointment);
    }

    // --- Happy path: Bank transfer ---
    @Test
    void shouldCheckoutWithBankTransfer() {
        // given
        var slotId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var studentEmail = "ana@test.com";
        var sessionNotes = "Help with integrals";
        var categoryPrice = new BigDecimal(30);

        var slot = mock(TimeSlot.class);
        var category = mock(ServiceCategory.class);
        var student = mock(Student.class);
        var command = mock(CheckoutCommand.class);
        var benefitApplication = mock(BenefitApplication.class);
        var appointment = mock(Appointment.class);
        var paymentInitiation = mock(PaymentInitiation.class);

        when(command.getReservedSlotId()).thenReturn(slotId);
        when(command.getServiceCategoryId()).thenReturn(categoryId);
        when(command.getSessionNotes()).thenReturn(sessionNotes);
        when(command.getPaymentMethod()).thenReturn(PaymentMethodChoice.BANK_TRANSFER);
        when(command.getGuestEmail()).thenReturn(studentEmail);
        when(slot.getState()).thenReturn(TimeSlotState.RESERVED);
        when(category.getPrice()).thenReturn(categoryPrice);
        when(benefitApplication.isApplied()).thenReturn(false);
        when(benefitApplication.getFinalPrice()).thenReturn(categoryPrice);

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(serviceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(studentRepository.findByEmail(studentEmail)).thenReturn(Optional.of(student));
        when(benefitService.apply(student, categoryPrice)).thenReturn(benefitApplication);
        when(appointmentRepository.save(any())).thenReturn(appointment);

        when(paymentService.initPayment(any())).thenReturn(paymentInitiation);
        when(appointmentRepository.findById(any())).thenReturn(Optional.of(appointment));

        // when
        checkoutService.checkout(command);

        // then
        verify(timeSlotRepository).findByIdForUpdate(slotId);
        verify(serviceCategoryRepository).findById(categoryId);
        verify(studentRepository).findByEmail(studentEmail);
        verify(benefitService).apply(student, categoryPrice);
        verify(appointmentRepository).save(any(Appointment.class));
        var captor = ArgumentCaptor.forClass(PaymentCommand.class);
        verify(paymentService).initPayment(captor.capture());

        var paymentCmd = captor.getValue();
        assertThat(paymentCmd.getPaymentMethod()).isEqualTo(PaymentMethodChoice.BANK_TRANSFER);
        assertThat(paymentCmd.getAppointment()).isEqualTo(appointment);

        verify(appointmentRepository).findById(any());
        verify(benefitService).consume(benefitApplication, appointment);
    }

    // --- Zero Price checkout ---
    @Test
    void shouldCheckoutWithZeroPrice() {
        // given
        var slotId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var appointmentId = UUID.randomUUID();
        var studentEmail = "ana@test.com";
        var sessionNotes = "Help with integrals";
        var categoryPrice = new BigDecimal(30);

        var slot = mock(TimeSlot.class);
        var category = mock(ServiceCategory.class);
        var student = mock(Student.class);
        var command = mock(CheckoutCommand.class);
        var benefitApplication = mock(BenefitApplication.class);
        var appointment = mock(Appointment.class);

        when(command.getReservedSlotId()).thenReturn(slotId);
        when(command.getServiceCategoryId()).thenReturn(categoryId);
        when(command.getSessionNotes()).thenReturn(sessionNotes);
        when(command.getPaymentMethod()).thenReturn(PaymentMethodChoice.STRIPE);
        when(command.getGuestEmail()).thenReturn(studentEmail);
        when(slot.getState()).thenReturn(TimeSlotState.RESERVED);
        when(category.getPrice()).thenReturn(categoryPrice);
        when(benefitApplication.isApplied()).thenReturn(true);
        when(benefitApplication.getType()).thenReturn(BenefitType.FREE_LESSON);
        when(benefitApplication.getFinalPrice()).thenReturn(BigDecimal.ZERO);
        when(appointment.getId()).thenReturn(appointmentId);
        when(appointment.getState()).thenReturn(AppointmentState.PAID);

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(serviceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(studentRepository.findByEmail(studentEmail)).thenReturn(Optional.of(student));
        when(benefitService.apply(student, categoryPrice)).thenReturn(benefitApplication);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        // when
        checkoutService.checkout(command);

        // then
        verify(timeSlotRepository).findByIdForUpdate(slotId);
        verify(serviceCategoryRepository).findById(categoryId);
        verify(studentRepository).findByEmail(studentEmail);

        verify(appointmentRepository, times(2)).save(any(Appointment.class));
        verify(appointmentStateMachine).transition(appointment, AppointmentState.PAID, "SYSTEM_ZERO_PRICE");
        verify(timeSlotStateMachine).transition(slot, TimeSlotState.BOOKED, "SYSTEM_ZERO_PRICE");
        verify(timeSlotRepository).save(slot);

        verify(appointmentRepository).findById(any());
        verify(benefitService).consume(benefitApplication, appointment);
        verify(paymentService, never()).initPayment(any());
    }

    // --- New student created ---
    @Test
    void shouldCreateStudentWhenNotFound() {
        // given
        var slotId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var studentName = "Ana Kovačević";
        var studentEmail = "ana@test.com";
        var studentPhone = "+385 91 234 5678";
        var sessionNotes = "Help with integrals";
        var categoryPrice = new BigDecimal(30);

        var slot = mock(TimeSlot.class);
        var category = mock(ServiceCategory.class);
        var student = mock(Student.class);
        var command = mock(CheckoutCommand.class);
        var benefitApplication = mock(BenefitApplication.class);
        var appointment = mock(Appointment.class);
        var paymentInitiation = mock(PaymentInitiation.class);

        when(command.getReservedSlotId()).thenReturn(slotId);
        when(command.getServiceCategoryId()).thenReturn(categoryId);
        when(command.getSessionNotes()).thenReturn(sessionNotes);
        when(command.getPaymentMethod()).thenReturn(PaymentMethodChoice.STRIPE);
        when(command.getGuestName()).thenReturn(studentName);
        when(command.getGuestEmail()).thenReturn(studentEmail);
        when(command.getGuestPhone()).thenReturn(studentPhone);
        when(slot.getState()).thenReturn(TimeSlotState.RESERVED);
        when(category.getPrice()).thenReturn(categoryPrice);
        when(benefitApplication.isApplied()).thenReturn(false);
        when(benefitApplication.getFinalPrice()).thenReturn(categoryPrice);

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(serviceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(studentRepository.findByEmail(studentEmail)).thenReturn(Optional.empty());
        when(studentRepository.save(any())).thenReturn(student);
        when(benefitService.apply(student, categoryPrice)).thenReturn(benefitApplication);
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(paymentService.initPayment(any())).thenReturn(paymentInitiation);
        when(appointmentRepository.findById(any())).thenReturn(Optional.of(appointment));

        // when
        checkoutService.checkout(command);

        // then
        verify(timeSlotRepository).findByIdForUpdate(slotId);
        verify(serviceCategoryRepository).findById(categoryId);
        verify(studentRepository).findByEmail(studentEmail);

        var studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        var studentToSave = studentCaptor.getValue();
        assertThat(studentToSave.getName()).isEqualTo(studentName);
        assertThat(studentToSave.getEmail()).isEqualTo(studentEmail);
        assertThat(studentToSave.getPhone()).isEqualTo(studentPhone);

        verify(benefitService).apply(student, categoryPrice);
        verify(appointmentRepository).save(any(Appointment.class));

        var captor = ArgumentCaptor.forClass(PaymentCommand.class);
        verify(paymentService).initPayment(captor.capture());

        var paymentCmd = captor.getValue();
        assertThat(paymentCmd.getPaymentMethod()).isEqualTo(PaymentMethodChoice.STRIPE);
        assertThat(paymentCmd.getAppointment()).isEqualTo(appointment);

        verify(appointmentRepository).findById(any());
        verify(benefitService).consume(benefitApplication, appointment);
    }

    // --- Slot not found ---
    @Test
    void shouldThrowWhenSlotNotFound() {
        // given
        var slotId = UUID.randomUUID();
        var command = mock(CheckoutCommand.class);

        when(command.getReservedSlotId()).thenReturn(slotId);
        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(command))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(appointmentRepository, never()).save(any());
    }

    // --- Slot not in RESERVED state ---
    @Test
    void shouldThrowWhenSlotNotReserved() {
        // given
        var slotId = UUID.randomUUID();
        var slot = mock(TimeSlot.class);
        var command = mock(CheckoutCommand.class);

        when(command.getReservedSlotId()).thenReturn(slotId);
        when(slot.getState()).thenReturn(TimeSlotState.AVAILABLE);

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(command))
                .isInstanceOf(InvalidReservationException.class);
        verify(appointmentRepository, never()).save(any());
    }

    // --- Category not found ---
    @Test
    void shouldThrowWhenCategoryNotFound() {
        // given
        var slotId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var slot = mock(TimeSlot.class);
        var command = mock(CheckoutCommand.class);

        when(command.getReservedSlotId()).thenReturn(slotId);
        when(command.getServiceCategoryId()).thenReturn(categoryId);
        when(slot.getState()).thenReturn(TimeSlotState.RESERVED);
        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(serviceCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(command))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(appointmentRepository, never()).save(any());
    }

    // --- Helpers ---
    private CheckoutCommand defaultCommand(UUID slotId, UUID categoryId, PaymentMethodChoice method) {
        return CheckoutCommand.builder()
                .reservedSlotId(slotId)
                .serviceCategoryId(categoryId)
                .guestName("Ana Kovačević")
                .guestEmail("ana@test.com")
                .guestPhone("+385 91 234 5678")
                .sessionNotes("Help with integrals")
                .paymentMethod(method)
                .build();
    }
}