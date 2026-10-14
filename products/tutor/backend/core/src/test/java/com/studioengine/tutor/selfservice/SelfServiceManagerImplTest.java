package com.studioengine.tutor.selfservice;


import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.CancellationToken;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.enums.TokenType;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.CancellationTokenRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.email.EmailService;
import com.studioengine.tutor.errors.exceptions.DeadlinePassedException;
import com.studioengine.tutor.errors.exceptions.PreBookedSelfServiceException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.TokenExpiredException;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfServiceManagerImplTest {

    private static final String ZONE_ID = "Europe/Zagreb";
    private static final int YEAR_NOW = 2026;
    private static final int MONTH_NOW = 9;
    private static final int DAY_NOW = 22;
    private static final int HOUR_NOW = 12;
    private static final int MIN_NOW = 0;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(YEAR_NOW, MONTH_NOW, DAY_NOW, HOUR_NOW, MIN_NOW); // 2026-09-22 12:00

    @Mock
    private CancellationTokenRepository cancellationTokenRepository;
    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private AppointmentStateMachine appointmentStateMachine;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TimeSlotStateMachine timeSlotStateMachine;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private TokenService tokenService;
    private Clock clock;

    private SelfServiceManagerImpl selfServiceManager;

    @BeforeEach
    void setUp() {
        var zone = ZoneId.of(ZONE_ID);
        var fixedNow = LocalDateTime.of(YEAR_NOW, MONTH_NOW, DAY_NOW, HOUR_NOW, MIN_NOW).atZone(zone).toInstant();
        clock = Clock.fixed(fixedNow, zone);
        selfServiceManager = new SelfServiceManagerImpl(
                cancellationTokenRepository,
                schedulingProperties,
                appointmentStateMachine,
                appointmentRepository,
                timeSlotStateMachine,
                timeSlotRepository,
                emailService,
                tokenService,
                clock
        );
    }

    @Nested
    class ValidateTokenTests {

        private static final int cancellationDeadline = 24; // hours

        @ParameterizedTest
        @MethodSource("validateTokenScenarios")
        void shouldValidateNotUsedNotExpiredNotForPreBooked(ValidateTokenScenario validateTokenScenario) {
            // given
            var token = UUID.randomUUID().toString();
            var studentName = "Marko Markić";
            var serviceCategoryName = "Pripreme za maturu";
            var appointmentId = UUID.randomUUID();
            var timeSlotDate = validateTokenScenario.date;
            var timeSlotStartTime = validateTokenScenario.time;
            var cancellationToken = mock(CancellationToken.class);
            var appointment = mock(Appointment.class);
            var slot = mock(TimeSlot.class);
            var student = mock(Student.class);
            var serviceCategory = mock(ServiceCategory.class);

            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(false);
            when(cancellationToken.getAppointment()).thenReturn(appointment);
            when(appointment.getId()).thenReturn(appointmentId);
            when(appointment.getState()).thenReturn(AppointmentState.PENDING_PAYMENT);
            when(appointment.getTimeSlot()).thenReturn(slot);
            when(appointment.getStudent()).thenReturn(student);
            when(appointment.getServiceCategory()).thenReturn(serviceCategory);
            when(slot.getSlotDate()).thenReturn(timeSlotDate);
            when(slot.getStartTime()).thenReturn(timeSlotStartTime);
            when(student.getName()).thenReturn(studentName);
            when(serviceCategory.getName()).thenReturn(serviceCategoryName);

            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(schedulingProperties.getCancellationDeadline()).thenReturn(Duration.ofHours(cancellationDeadline));

            // then
            var result = selfServiceManager.validateToken(token);

            // when
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties).getCancellationDeadline();

            assertThat(result.getAppointmentId()).isEqualTo(appointmentId);
            assertThat(result.getStudentName()).isEqualTo(studentName);
            assertThat(result.getServiceCategoryName()).isEqualTo(serviceCategoryName);
            assertThat(result.getDate()).isEqualTo(timeSlotDate);
            assertThat(result.getStartTime()).isEqualTo(timeSlotStartTime);
            assertThat(result.isDeadlineMissed()).isEqualTo(validateTokenScenario.deadlinePassed());
        }

        @Test
        void shouldNotValidateWhenTokenNotExists() {
            // given
            var token = UUID.randomUUID().toString();

            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> selfServiceManager.validateToken(token)).isInstanceOf(ResourceNotFoundException.class);

            // when
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties, never()).getCancellationDeadline();
        }

        @Test
        void shouldValidateUsedToken() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);

            when(cancellationToken.isUsed()).thenReturn(true);

            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));

            // then
            assertThatThrownBy(() -> selfServiceManager.validateToken(token)).isInstanceOf(TokenExpiredException.class);

            // when
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties, never()).getCancellationDeadline();
        }

        @Test
        void shouldValidateUnusedExpiredToken() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);

            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(true);

            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));

            // then
            assertThatThrownBy(() -> selfServiceManager.validateToken(token)).isInstanceOf(TokenExpiredException.class);

            // when
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties, never()).getCancellationDeadline();
        }

        @Test
        void shouldValidateUnusedNotExpiredPreBookedToken() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            var appointment = mock(Appointment.class);

            when(appointment.getState()).thenReturn(AppointmentState.PRE_BOOKED);
            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(false);
            when(cancellationToken.getAppointment()).thenReturn(appointment);

            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));

            // when
            assertThatThrownBy(() -> selfServiceManager.validateToken(token)).isInstanceOf(PreBookedSelfServiceException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties, never()).getCancellationDeadline();
        }

        record ValidateTokenScenario(LocalDate date, LocalTime time, boolean deadlinePassed) {}

        private static Stream<ValidateTokenScenario> validateTokenScenarios() {
            // slot start just BEYOND the 24h window -> deadline NOT passed -> deadlineMissed=false
            var notMissed = FIXED_NOW.plusHours(cancellationDeadline).plusMinutes(1);
            // slot start just INSIDE the 24h window -> deadline PASSED -> deadlineMissed=true
            var missed = FIXED_NOW.plusHours(cancellationDeadline).minusMinutes(1);
            return Stream.of(
                    new ValidateTokenScenario(notMissed.toLocalDate(), notMissed.toLocalTime(), false),
                    new ValidateTokenScenario(missed.toLocalDate(), missed.toLocalTime(), true)
            );
        }
    }

    @Nested
    class CancellationTests {

        private static final int cancellationDeadline = 24; // hours

        @Test
        void shouldConfirmCancellation() {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentId = UUID.randomUUID();
            var slotStartDateTime = FIXED_NOW.plusHours(cancellationDeadline).plusMinutes(1);
            var slotDate = slotStartDateTime.toLocalDate();
            var slotStartTime = slotStartDateTime.toLocalTime();
            var cancellationToken = mock(CancellationToken.class);
            var appointment = mock(Appointment.class);
            var slot = mock(TimeSlot.class);

            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(false);
            when(cancellationToken.getAppointment()).thenReturn(appointment);
            when(appointment.getState()).thenReturn(AppointmentState.PENDING_PAYMENT);
            when(appointment.getId()).thenReturn(appointmentId);
            when(appointment.getTimeSlot()).thenReturn(slot);
            when(slot.getSlotDate()).thenReturn(slotDate);
            when(slot.getStartTime()).thenReturn(slotStartTime);
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(schedulingProperties.getCancellationDeadline()).thenReturn(Duration.ofHours(cancellationDeadline));

            // when
            var result = selfServiceManager.confirmCancellation(token);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties).getCancellationDeadline();
            verify(appointmentStateMachine).transition(appointment, AppointmentState.CANCELLED, "STUDENT_SELF_SERVICE");
            verify(timeSlotStateMachine).transition(slot, TimeSlotState.AVAILABLE, "STUDENT_SELF_SERVICE");
            verify(appointmentRepository).save(appointment);
            verify(timeSlotRepository).save(slot);
            verify(cancellationToken).markUsed();
            verify(cancellationTokenRepository).save(cancellationToken);
            verify(emailService).sendCancellationNotification(appointment, "Student self-cancellation");

            assertThat(result.getAppointmentId()).isEqualTo(appointmentId);
            assertThat(result.getMessage()).isEqualTo("Appointment cancelled");
        }

        @Test
        void shouldNotConfirmCancellationWhenTokenNotExists() {
            // given
            var token = UUID.randomUUID().toString();
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmCancellation(token)).isInstanceOf(ResourceNotFoundException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }

        @Test
        void shouldNotConfirmCancellationWhenTokenIsUsed() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(true);

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmCancellation(token)).isInstanceOf(TokenExpiredException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }

        @Test
        void shouldNotConfirmCancellationWhenTokenExpired() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(true);

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmCancellation(token)).isInstanceOf(TokenExpiredException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }

        @Test
        void shouldNotConfirmCancellationWhenAppointmentPreBooked() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            var appointment = mock(Appointment.class);
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(false);
            when(cancellationToken.getAppointment()).thenReturn(appointment);
            when(appointment.getState()).thenReturn(AppointmentState.PRE_BOOKED);

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmCancellation(token)).isInstanceOf(PreBookedSelfServiceException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }

        @Test
        void shouldNotConfirmCancellationWhenDeadlinePassed() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            var appointment = mock(Appointment.class);
            var slot = mock(TimeSlot.class);
            var passedDeadline = FIXED_NOW.plusHours(cancellationDeadline).minusMinutes(2);
            var slotDate = passedDeadline.toLocalDate();
            var slotStartTime = passedDeadline.toLocalTime();
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(false);
            when(cancellationToken.getAppointment()).thenReturn(appointment);
            when(appointment.getState()).thenReturn(AppointmentState.PENDING_PAYMENT);
            when(appointment.getTimeSlot()).thenReturn(slot);
            when(slot.getSlotDate()).thenReturn(slotDate);
            when(slot.getStartTime()).thenReturn(slotStartTime);
            when(schedulingProperties.getCancellationDeadline()).thenReturn(Duration.ofHours(cancellationDeadline));

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmCancellation(token)).isInstanceOf(DeadlinePassedException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }
    }

    @Nested
    class RescheduleTests {

        private static final int cancellationDeadline = 24;

        @Test
        void shouldConfirmReschedule() {
            // given
            var token = UUID.randomUUID().toString();
            var slotStartDateTime = FIXED_NOW.plusHours(cancellationDeadline).plusMinutes(1);
            var slotDate = slotStartDateTime.toLocalDate();
            var slotStartTime = slotStartDateTime.toLocalTime();
            var appointmentId = UUID.randomUUID();
            var cancellationToken = mock(CancellationToken.class);
            var appointment = mock(Appointment.class);
            var slot = mock(TimeSlot.class);

            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(false);
            when(cancellationToken.getAppointment()).thenReturn(appointment);
            when(appointment.getId()).thenReturn(appointmentId);
            when(appointment.getState()).thenReturn(AppointmentState.PENDING_PAYMENT);
            when(appointment.getTimeSlot()).thenReturn(slot);
            when(schedulingProperties.getCancellationDeadline()).thenReturn(Duration.ofHours(cancellationDeadline));
            when(slot.getSlotDate()).thenReturn(slotDate);
            when(slot.getStartTime()).thenReturn(slotStartTime);

            // when
            var result = selfServiceManager.confirmReschedule(token);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties).getCancellationDeadline();
            verify(appointmentStateMachine).transition(appointment, AppointmentState.CANCELLED, "STUDENT_SELF_SERVICE");
            verify(timeSlotStateMachine).transition(slot, TimeSlotState.AVAILABLE, "STUDENT_SELF_SERVICE");
            verify(appointmentRepository).save(appointment);
            verify(timeSlotRepository).save(slot);
            verify(cancellationToken).markUsed();

            var captor = ArgumentCaptor.forClass(CancellationToken.class);
            verify(cancellationTokenRepository, times(2)).save(captor.capture());
            var savedTokens = captor.getAllValues();
            var originalToken = savedTokens.get(0); // the marked used one
            var rescheduleToken = savedTokens.get(1); // the new one
            assertThat(rescheduleToken.getAppointment()).isEqualTo(appointment);
            assertThat(originalToken).isEqualTo(cancellationToken);
            assertThat(rescheduleToken.getTokenType()).isEqualTo(TokenType.RESCHEDULE_BOOKING);

            assertThat(result.getOriginalAppointmentId()).isEqualTo(appointmentId);
        }

        @Test
        void shouldNotRescheduleWhenTokenNotExists() {
            // given
            var token = UUID.randomUUID().toString();
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmReschedule(token)).isInstanceOf(ResourceNotFoundException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties, never()).getCancellationDeadline();
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }

        @Test
        void shouldNotRescheduleWhenTokenIsUsed() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(true);

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmReschedule(token)).isInstanceOf(TokenExpiredException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties, never()).getCancellationDeadline();
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }

        @Test
        void shouldNotRescheduleWhenTokenIsExpired() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(true);

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmReschedule(token)).isInstanceOf(TokenExpiredException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties, never()).getCancellationDeadline();
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }

        @Test
        void shouldNotRescheduleWhenAppointmentIsPreBooked() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            var appointment = mock(Appointment.class);
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(false);
            when(cancellationToken.getAppointment()).thenReturn(appointment);
            when(appointment.getState()).thenReturn(AppointmentState.PRE_BOOKED);

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmReschedule(token)).isInstanceOf(PreBookedSelfServiceException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties, never()).getCancellationDeadline();
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }

        @Test
        void shouldNotRescheduleWhenDeadlinePassed() {
            // given
            var token = UUID.randomUUID().toString();
            var cancellationToken = mock(CancellationToken.class);
            var appointment = mock(Appointment.class);
            var slot = mock(TimeSlot.class);
            var passedDeadline = FIXED_NOW.plusHours(cancellationDeadline).minusMinutes(2);
            var slotDate = passedDeadline.toLocalDate();
            var slotStartTime = passedDeadline.toLocalTime();
            when(cancellationTokenRepository.findByToken(token)).thenReturn(Optional.of(cancellationToken));
            when(cancellationToken.isUsed()).thenReturn(false);
            when(cancellationToken.isExpired()).thenReturn(false);
            when(cancellationToken.getAppointment()).thenReturn(appointment);
            when(appointment.getState()).thenReturn(AppointmentState.PENDING_PAYMENT);
            when(appointment.getTimeSlot()).thenReturn(slot);
            when(slot.getSlotDate()).thenReturn(slotDate);
            when(slot.getStartTime()).thenReturn(slotStartTime);
            when(schedulingProperties.getCancellationDeadline()).thenReturn(Duration.ofHours(cancellationDeadline));

            // when
            assertThatThrownBy(() -> selfServiceManager.confirmReschedule(token)).isInstanceOf(DeadlinePassedException.class);

            // then
            verify(cancellationTokenRepository).findByToken(token);
            verify(schedulingProperties).getCancellationDeadline();
            verify(appointmentStateMachine, never()).transition(any(), any(), any());
        }
    }

    @Nested
    class CompleteRescheduleTests {

        @ParameterizedTest
        @MethodSource("paidLikeStates")
        void shouldCompleteRescheduleForPaidLikeStateAndBookSlot(AppointmentState originalState) {
            // given
            var slotId = UUID.randomUUID();
            var rescheduleToken = UUID.randomUUID().toString();
            var token = mock(CancellationToken.class);
            var originalAppointment = mock(Appointment.class);
            var student = mock(Student.class);
            var category = mock(ServiceCategory.class);
            var newSlot = mock(TimeSlot.class);

            when(cancellationTokenRepository.findByToken(rescheduleToken)).thenReturn(Optional.of(token));
            when(token.isUsed()).thenReturn(false);
            when(token.isExpired()).thenReturn(false);
            when(token.getAppointment()).thenReturn(originalAppointment);
            when(originalAppointment.getState()).thenReturn(originalState);
            when(originalAppointment.getStudent()).thenReturn(student);
            when(originalAppointment.getServiceCategory()).thenReturn(category);
            when(originalAppointment.getOriginalPrice()).thenReturn(new BigDecimal("30.00"));
            when(originalAppointment.getFinalPrice()).thenReturn(new BigDecimal("30.00"));
            when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(newSlot));
            when(newSlot.getState()).thenReturn(TimeSlotState.AVAILABLE);
            when(tokenService.generateManageLink(any(Appointment.class))).thenReturn("http://localhost:8080/manage.html?token=x");

            // when
            selfServiceManager.completeReschedule(slotId, rescheduleToken);

            // then
            verify(timeSlotStateMachine).transition(newSlot, TimeSlotState.BOOKED, "RESCHEDULE");
            verify(timeSlotRepository).save(newSlot);
            verify(appointmentRepository).save(any(Appointment.class));
            verify(token).markUsed();
            verify(emailService).sendConfirmationEmail(any(Appointment.class), any(String.class));
            verify(emailService).sendRescheduleNotification(eq(originalAppointment), any(Appointment.class));
            verify(emailService, never()).sendPendingPaymentEmail(any(), any());
        }

        static Stream<AppointmentState> paidLikeStates() {
            return Stream.of(AppointmentState.PAID, AppointmentState.CONFIRMED);
        }

        @Test
        void shouldCompleteRescheduleForPendingPaymentAndReserveSlot() {
            // given
            var slotId = UUID.randomUUID();
            var rescheduleToken = UUID.randomUUID().toString();
            var token = mock(CancellationToken.class);
            var originalAppointment = mock(Appointment.class);
            var student = mock(Student.class);
            var category = mock(ServiceCategory.class);
            var newSlot = mock(TimeSlot.class);

            when(cancellationTokenRepository.findByToken(rescheduleToken)).thenReturn(Optional.of(token));
            when(token.isUsed()).thenReturn(false);
            when(token.isExpired()).thenReturn(false);
            when(token.getAppointment()).thenReturn(originalAppointment);
            when(originalAppointment.getState()).thenReturn(AppointmentState.PENDING_PAYMENT);
            when(originalAppointment.getStudent()).thenReturn(student);
            when(originalAppointment.getServiceCategory()).thenReturn(category);
            when(originalAppointment.getOriginalPrice()).thenReturn(new BigDecimal("30.00"));
            when(originalAppointment.getFinalPrice()).thenReturn(new BigDecimal("30.00"));
            when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(newSlot));
            when(newSlot.getState()).thenReturn(TimeSlotState.AVAILABLE);
            when(tokenService.generateManageLink(any(Appointment.class))).thenReturn("http://localhost:8080/manage.html?token=x");

            // when
            selfServiceManager.completeReschedule(slotId, rescheduleToken);

            // then
            verify(timeSlotStateMachine).transition(newSlot, TimeSlotState.RESERVED, "RESCHEDULE");
            verify(timeSlotRepository).save(newSlot);
            verify(appointmentRepository).save(any(Appointment.class));
            verify(token).markUsed();
            verify(emailService).sendPendingPaymentEmail(any(Appointment.class), any(String.class));
            verify(emailService).sendRescheduleNotification(eq(originalAppointment), any(Appointment.class));
            verify(emailService, never()).sendConfirmationEmail(any(), any());
        }

        @Test
        void shouldNotCompleteRescheduleWhenTokenNotFound() {
            // given
            var slotId = UUID.randomUUID();
            var rescheduleToken = UUID.randomUUID().toString();
            when(cancellationTokenRepository.findByToken(rescheduleToken)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> selfServiceManager.completeReschedule(slotId, rescheduleToken))
                    .isInstanceOf(TokenExpiredException.class);
            verify(timeSlotStateMachine, never()).transition(any(), any(), any());
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        void shouldNotCompleteRescheduleWhenTokenUsed() {
            // given
            var slotId = UUID.randomUUID();
            var rescheduleToken = UUID.randomUUID().toString();
            var token = mock(CancellationToken.class);
            when(cancellationTokenRepository.findByToken(rescheduleToken)).thenReturn(Optional.of(token));
            when(token.isUsed()).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> selfServiceManager.completeReschedule(slotId, rescheduleToken))
                    .isInstanceOf(TokenExpiredException.class);
            verify(timeSlotStateMachine, never()).transition(any(), any(), any());
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        void shouldNotCompleteRescheduleWhenSlotNotAvailable() {
            // given
            var slotId = UUID.randomUUID();
            var rescheduleToken = UUID.randomUUID().toString();
            var token = mock(CancellationToken.class);
            var originalAppointment = mock(Appointment.class);
            var student = mock(Student.class);
            var category = mock(ServiceCategory.class);
            var newSlot = mock(TimeSlot.class);
            when(cancellationTokenRepository.findByToken(rescheduleToken)).thenReturn(Optional.of(token));
            when(token.isUsed()).thenReturn(false);
            when(token.isExpired()).thenReturn(false);
            when(token.getAppointment()).thenReturn(originalAppointment);
            when(originalAppointment.getStudent()).thenReturn(student);
            when(originalAppointment.getServiceCategory()).thenReturn(category);
            when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(newSlot));
            when(newSlot.getState()).thenReturn(TimeSlotState.RESERVED);

            // when / then
            assertThatThrownBy(() -> selfServiceManager.completeReschedule(slotId, rescheduleToken))
                    .isInstanceOf(IllegalStateException.class);
            verify(timeSlotStateMachine, never()).transition(any(), any(), any());
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        void shouldNotCompleteRescheduleWhenOriginalStateNotReschedulable() {
            // given
            var slotId = UUID.randomUUID();
            var rescheduleToken = UUID.randomUUID().toString();
            var token = mock(CancellationToken.class);
            var originalAppointment = mock(Appointment.class);
            var student = mock(Student.class);
            var category = mock(ServiceCategory.class);
            var newSlot = mock(TimeSlot.class);
            when(cancellationTokenRepository.findByToken(rescheduleToken)).thenReturn(Optional.of(token));
            when(token.isUsed()).thenReturn(false);
            when(token.isExpired()).thenReturn(false);
            when(token.getAppointment()).thenReturn(originalAppointment);
            when(originalAppointment.getState()).thenReturn(AppointmentState.COMPLETED);
            when(originalAppointment.getStudent()).thenReturn(student);
            when(originalAppointment.getServiceCategory()).thenReturn(category);
            when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(newSlot));
            when(newSlot.getState()).thenReturn(TimeSlotState.AVAILABLE);

            // when / then
            assertThatThrownBy(() -> selfServiceManager.completeReschedule(slotId, rescheduleToken))
                    .isInstanceOf(IllegalStateException.class);
            verify(timeSlotStateMachine, never()).transition(any(), any(), any());
            verify(appointmentRepository, never()).save(any());
        }
    }
}