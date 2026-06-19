package com.studioengine.tutor.checkout;

import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    // --- Happy path: Stripe ---
    @Test
    void shouldCheckoutWithStripePayment() {
        // given
        var slotId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var slot = createReservedSlot(slotId);
        var category = createCategory(categoryId, new BigDecimal("30.00"));
        var student = createStudent();
        var command = defaultCommand(slotId, categoryId, PaymentMethodChoice.STRIPE);

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(serviceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(studentRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(student));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.initPayment(any())).thenReturn(
                PaymentInitiation.builder()
                        .appointmentId(UUID.randomUUID())
                        .resultingState(AppointmentState.RESERVED)
                        .build()
        );

        // when
        checkoutService.checkout(command);

        // then
        var captor = ArgumentCaptor.forClass(PaymentCommand.class);
        verify(paymentService).initPayment(captor.capture());

        var paymentCmd = captor.getValue();
        assertThat(paymentCmd.getPaymentMethod()).isEqualTo(PaymentMethodChoice.STRIPE);
        assertThat(paymentCmd.getAppointment().getServiceCategory()).isEqualTo(category);
        assertThat(paymentCmd.getAppointment().getStudent()).isEqualTo(student);
        assertThat(paymentCmd.getAppointment().getTimeSlot()).isEqualTo(slot);
        assertThat(paymentCmd.getAppointment().getFinalPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(paymentCmd.getAppointment().getState()).isEqualTo(AppointmentState.RESERVED);
    }

    // --- Happy path: Bank transfer ---
    @Test
    void shouldCheckoutWithBankTransfer() {
        // given
        var slotId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var slot = createReservedSlot(slotId);
        var category = createCategory(categoryId, new BigDecimal("30.00"));
        var student = createStudent();
        var command = defaultCommand(slotId, categoryId, PaymentMethodChoice.BANK_TRANSFER);

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(serviceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(studentRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(student));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.initPayment(any())).thenReturn(
                PaymentInitiation.builder()
                        .appointmentId(UUID.randomUUID())
                        .resultingState(AppointmentState.PENDING_PAYMENT)
                        .build()
        );

        // when
        checkoutService.checkout(command);

        // then
        var captor = ArgumentCaptor.forClass(PaymentCommand.class);
        verify(paymentService).initPayment(captor.capture());

        var paymentCmd = captor.getValue();
        assertThat(paymentCmd.getPaymentMethod()).isEqualTo(PaymentMethodChoice.BANK_TRANSFER);
        assertThat(paymentCmd.getAppointment().getServiceCategory()).isEqualTo(category);
        assertThat(paymentCmd.getAppointment().getStudent()).isEqualTo(student);
        assertThat(paymentCmd.getAppointment().getTimeSlot()).isEqualTo(slot);
        assertThat(paymentCmd.getAppointment().getFinalPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(paymentCmd.getAppointment().getState()).isEqualTo(AppointmentState.RESERVED);
    }

    // --- New student created ---
    @Test
    void shouldCreateStudentWhenNotFound() {
        // given
        var slotId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var slot = createReservedSlot(slotId);
        var category = createCategory(categoryId, new BigDecimal("30.00"));
        var command = defaultCommand(slotId, categoryId, PaymentMethodChoice.STRIPE);

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(serviceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(studentRepository.findByEmail("ana@test.com")).thenReturn(Optional.empty());
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.initPayment(any())).thenReturn(
                PaymentInitiation.builder()
                        .appointmentId(UUID.randomUUID())
                        .resultingState(AppointmentState.RESERVED)
                        .build()
        );

        // when
        checkoutService.checkout(command);

        // then
        var captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@test.com");
        assertThat(captor.getValue().getName()).isEqualTo("Ana Kovačević");
    }

    // --- Slot not found ---
    @Test
    void shouldThrowWhenSlotNotFound() {
        // given
        var slotId = UUID.randomUUID();
        var command = defaultCommand(slotId, UUID.randomUUID(), PaymentMethodChoice.STRIPE);

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
        var slot = createSlotInState(slotId, TimeSlotState.AVAILABLE);
        var command = defaultCommand(slotId, UUID.randomUUID(), PaymentMethodChoice.STRIPE);

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
        var slot = createReservedSlot(slotId);
        var command = defaultCommand(slotId, categoryId, PaymentMethodChoice.STRIPE);

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

    private TimeSlot createReservedSlot(UUID id) {
        return createSlotInState(id, TimeSlotState.RESERVED);
    }

    private TimeSlot createSlotInState(UUID id, TimeSlotState state) {
        var slot = TimeSlot.create(LocalDate.of(2026, 6, 20), LocalTime.of(10, 0));
        if (state != TimeSlotState.DRAFT) {
            slot.transitionTo(state);
        }
        setId(slot, id);
        return slot;
    }

    private ServiceCategory createCategory(UUID id, BigDecimal price) {
        var category = ServiceCategory.create("Math Lesson", "Algebra basics", price, "EUR");
        setId(category, id);
        return category;
    }

    private Student createStudent() {
        var student = Student.create("Ana Kovačević", "ana@test.com", "+385 91 234 5678");
        setId(student, UUID.randomUUID());
        return student;
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}