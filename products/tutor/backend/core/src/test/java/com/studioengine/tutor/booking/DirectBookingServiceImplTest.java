package com.studioengine.tutor.booking;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentOrigin;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.ServiceCategoryRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectBookingServiceImplTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TimeSlotStateMachine timeSlotStateMachine;

    @InjectMocks
    private DirectBookingServiceImpl directBookingService;

    @ParameterizedTest
    @MethodSource("allowedStates")
    void shouldBookWhenSlotInAllowedState(TimeSlotState allowedState) {
        // given
        var timeSlotId = UUID.randomUUID();
        var slotDate = LocalDate.of(2026, 6, 22);
        var startTime = LocalTime.of(10, 0);
        var timeSlot = mock(TimeSlot.class);
        var studentId = UUID.randomUUID();
        var studentName = "Marko Markić";
        var studentEmail = "marko.markic@example.com";
        var studentPhone = "+38599123456";
        var student = mock(Student.class);
        var serviceCategoryId = UUID.randomUUID();
        var serviceCategoryName = "Matematika za osnovnu školu";
        var serviceCategoryPrice = new BigDecimal(35);
        var serviceCategory = mock(ServiceCategory.class);
        var command = DirectBookingCommand.builder()
                .timeSlotId(timeSlotId)
                .studentId(studentId)
                .serviceCategoryId(serviceCategoryId)
                .build();

        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(timeSlot.getId()).thenReturn(timeSlotId);
        when(timeSlot.getState()).thenReturn(allowedState);
        when(timeSlot.getSlotDate()).thenReturn(slotDate);
        when(timeSlot.getStartTime()).thenReturn(startTime);
        when(student.getId()).thenReturn(studentId);
        when(student.getName()).thenReturn(studentName);
        when(student.getEmail()).thenReturn(studentEmail);
        when(student.getPhone()).thenReturn(studentPhone);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(serviceCategory.getId()).thenReturn(serviceCategoryId);
        when(serviceCategory.getName()).thenReturn(serviceCategoryName);
        when(serviceCategory.getPrice()).thenReturn(serviceCategoryPrice);
        when(serviceCategoryRepository.findById(serviceCategoryId)).thenReturn(Optional.of(serviceCategory));

        // when
        var result = directBookingService.book(command);

        // then
        var captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        var appointmentValue = captor.getValue();
        assertThat(appointmentValue.getTimeSlot()).isEqualTo(timeSlot);
        assertThat(appointmentValue.getServiceCategory()).isEqualTo(serviceCategory);
        assertThat(appointmentValue.getStudent()).isEqualTo(student);
        assertThat(appointmentValue.getState()).isEqualTo(AppointmentState.PRE_BOOKED);
        assertThat(appointmentValue.getOriginalPrice()).isEqualTo(serviceCategory.getPrice());
        assertThat(appointmentValue.getFinalPrice()).isEqualTo(serviceCategory.getPrice());
        assertThat(appointmentValue.getOrigin()).isEqualTo(AppointmentOrigin.DASHBOARD_DIRECT);
        assertThat(appointmentValue.getSessionNotes()).isNull();

        verify(timeSlotStateMachine).transition(timeSlot, TimeSlotState.PRE_BOOKED, "TUTOR");
        verify(timeSlotRepository).save(timeSlot);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotBookWhenSlotNotFound() {
        // given
        var timeSlotId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var serviceCategoryId = UUID.randomUUID();

        var command = DirectBookingCommand.builder()
                .timeSlotId(timeSlotId)
                .studentId(studentId)
                .serviceCategoryId(serviceCategoryId)
                .build();

        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> directBookingService.book(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(timeSlotRepository).findById(timeSlotId);
        verify(studentRepository, never()).findById(studentId);
    }

    @ParameterizedTest
    @MethodSource("unallowedStates")
    void shouldNotBookWhenSlotInUnAllowedState(TimeSlotState unallowedState) {
        // given
        var timeSlotId = UUID.randomUUID();
        var timeSlot = mock(TimeSlot.class);
        var studentId = UUID.randomUUID();
        var serviceCategoryId = UUID.randomUUID();

        var command = DirectBookingCommand.builder()
                .timeSlotId(timeSlotId)
                .studentId(studentId)
                .serviceCategoryId(serviceCategoryId)
                .build();

        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(timeSlot.getState()).thenReturn(unallowedState);

        // when
        assertThatThrownBy(() -> directBookingService.book(command)).isInstanceOf(InvalidStateTransitionException.class);

        // then
        verify(timeSlotRepository).findById(timeSlotId);
        verify(studentRepository, never()).findById(any());
    }

    @Test
    void shouldNotBookWhenStudentNotFound() {
        // given
        var timeSlotId = UUID.randomUUID();
        var timeSlot = mock(TimeSlot.class);
        var studentId = UUID.randomUUID();
        var serviceCategoryId = UUID.randomUUID();

        var command = DirectBookingCommand.builder()
                .timeSlotId(timeSlotId)
                .studentId(studentId)
                .serviceCategoryId(serviceCategoryId)
                .build();

        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(timeSlot.getState()).thenReturn(TimeSlotState.DRAFT);
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> directBookingService.book(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(timeSlotRepository).findById(timeSlotId);
        verify(studentRepository).findById(studentId);
        verify(serviceCategoryRepository, never()).findById(any());
    }

    @Test
    void shouldNotBookWhenServiceCategoryNotFound() {
        // given
        var timeSlotId = UUID.randomUUID();
        var timeSlot = mock(TimeSlot.class);
        var studentId = UUID.randomUUID();
        var student = mock(Student.class);
        var serviceCategoryId = UUID.randomUUID();

        var command = DirectBookingCommand.builder()
                .timeSlotId(timeSlotId)
                .studentId(studentId)
                .serviceCategoryId(serviceCategoryId)
                .build();

        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(timeSlot.getState()).thenReturn(TimeSlotState.DRAFT);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(serviceCategoryRepository.findById(serviceCategoryId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> directBookingService.book(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(timeSlotRepository).findById(timeSlotId);
        verify(studentRepository).findById(studentId);
        verify(serviceCategoryRepository).findById(serviceCategoryId);
        verify(appointmentRepository, never()).save(any());
    }

    static Stream<TimeSlotState> allowedStates() {
        return Stream.of(TimeSlotState.DRAFT, TimeSlotState.AVAILABLE);
    }

    static Stream<TimeSlotState> unallowedStates() {
        return Stream.of(TimeSlotState.RESERVED, TimeSlotState.BOOKED, TimeSlotState.PRE_BOOKED);
    }
}