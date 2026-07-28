package com.studioengine.tutor.landing;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LandingServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BrandProperties brandProperties;

    @InjectMocks
    private LandingServiceImpl landingService;

    @Nested
    class AwaitingClosureTests {

        @Test
        void shouldReturnAwaitingClosure() {
            // given
            var appointmentId = UUID.randomUUID();
            var studentName = "Marko Markić";
            var serviceCategoryName = "Primary school Math";
            var finalPrice = BigDecimal.TEN;
            var slotDate = LocalDate.now().minusDays(2);
            var startTime = LocalTime.of(10, 0);
            var appointment = mockAppointment(appointmentId, studentName, serviceCategoryName, slotDate, startTime, finalPrice);

            when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
            when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of(appointment));
            when(appointmentRepository.findByStateIn(any())).thenReturn(List.of());
            when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of());

            // when
            var result = landingService.getLandingPageData();

            // then
            assertThat(result.getAwaitingClosure()).hasSize(1);
            var item = result.getAwaitingClosure().getFirst();
            assertThat(item.getAppointmentId()).isEqualTo(appointmentId);
            assertThat(item.getStudentName()).isEqualTo(studentName);
            assertThat(item.getServiceCategory()).isEqualTo(serviceCategoryName);
            assertThat(item.getDate()).isEqualTo(slotDate);
            assertThat(item.getStartTime()).isEqualTo(startTime);
            assertThat(item.getAmount()).isEqualTo(finalPrice);
        }

        @Test
        void shouldReturnEmptyAwaitingClosure() {
            // given
            when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
            when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());
            when(appointmentRepository.findByStateIn(any())).thenReturn(List.of());
            when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of());

            // when
            var result = landingService.getLandingPageData();

            // then
            assertThat(result.getAwaitingClosure()).isEmpty();
        }
    }

    @Nested
    class PendingPaymentsTests {

        @Test
        void shouldReturnPendingPayments() {
            // given
            var appointmentId = UUID.randomUUID();
            var studentName = "Ana Kovačević";
            var serviceCategoryName = "Državna Matura Prep";
            var finalPrice = BigDecimal.valueOf(35);
            var slotDate = LocalDate.now().plusDays(1);
            var startTime = LocalTime.of(14, 0);
            var appointment = mockAppointment(appointmentId, studentName, serviceCategoryName, slotDate, startTime, finalPrice);

            when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
            when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());
            when(appointmentRepository.findByStateIn(any())).thenReturn(List.of(appointment));
            when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of());

            // when
            var result = landingService.getLandingPageData();

            // then
            assertThat(result.getPendingPayments()).hasSize(1);
            var item = result.getPendingPayments().getFirst();
            assertThat(item.getAppointmentId()).isEqualTo(appointmentId);
            assertThat(item.getStudentName()).isEqualTo(studentName);
            assertThat(item.getAmount()).isEqualTo(finalPrice);
        }

        @Test
        void shouldReturnEmptyPendingPayments() {
            // given
            when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
            when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());
            when(appointmentRepository.findByStateIn(any())).thenReturn(List.of());
            when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of());

            // when
            var result = landingService.getLandingPageData();

            // then
            assertThat(result.getPendingPayments()).isEmpty();
        }
    }

    @Nested
    class TodayUpcomingTests {

        @Test
        void shouldReturnTodayUpcoming() {
            // given
            var appointmentId = UUID.randomUUID();
            var studentName = "Petra Novak";
            var serviceCategoryName = "Fakultetska matematika";
            var finalPrice = BigDecimal.valueOf(40);
            var slotDate = LocalDate.now();
            var startTime = LocalTime.now().plusHours(2);
            var appointment = mockAppointment(appointmentId, studentName, serviceCategoryName, slotDate, startTime, finalPrice);

            when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
            when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());
            when(appointmentRepository.findByStateIn(any())).thenReturn(List.of());
            when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of(appointment));

            // when
            var result = landingService.getLandingPageData();

            // then
            assertThat(result.getTodayUpcoming()).hasSize(1);
            var item = result.getTodayUpcoming().getFirst();
            assertThat(item.getAppointmentId()).isEqualTo(appointmentId);
            assertThat(item.getStudentName()).isEqualTo(studentName);
            assertThat(item.getServiceCategory()).isEqualTo(serviceCategoryName);
            assertThat(item.getStartTime()).isEqualTo(startTime);
        }

        @Test
        void shouldFilterOutPastAppointmentsFromTodayUpcoming() {
            // given
            var pastAppointment = mockAppointment(
                    UUID.randomUUID(), "Past Student", "Math",
                    LocalDate.now(), LocalTime.now().minusHours(1), BigDecimal.TEN
            );
            var futureAppointment = mockAppointment(
                    UUID.randomUUID(), "Future Student", "Math",
                    LocalDate.now(), LocalTime.now().plusHours(2), BigDecimal.TEN
            );

            when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
            when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());
            when(appointmentRepository.findByStateIn(any())).thenReturn(List.of());
            when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of(pastAppointment, futureAppointment));

            // when
            var result = landingService.getLandingPageData();

            // then
            assertThat(result.getTodayUpcoming()).hasSize(1);
            assertThat(result.getTodayUpcoming().getFirst().getStudentName()).isEqualTo("Future Student");
        }

        @Test
        void shouldReturnEmptyTodayUpcoming() {
            // given
            when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
            when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());
            when(appointmentRepository.findByStateIn(any())).thenReturn(List.of());
            when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of());

            // when
            var result = landingService.getLandingPageData();

            // then
            assertThat(result.getTodayUpcoming()).isEmpty();
        }
    }

    // --- Helper ---

    private Appointment mockAppointment(UUID id, String studentName, String serviceCategoryName,
                                        LocalDate slotDate, LocalTime startTime, BigDecimal finalPrice) {
        var student = mock(Student.class);
        var serviceCategory = mock(ServiceCategory.class);
        var timeSlot = mock(TimeSlot.class);
        var appointment = mock(Appointment.class);

        lenient().when(appointment.getId()).thenReturn(id);
        lenient().when(appointment.getStudent()).thenReturn(student);
        lenient().when(student.getName()).thenReturn(studentName);
        lenient().when(appointment.getServiceCategory()).thenReturn(serviceCategory);
        lenient().when(serviceCategory.getName()).thenReturn(serviceCategoryName);
        lenient().when(appointment.getTimeSlot()).thenReturn(timeSlot);
        lenient().when(timeSlot.getSlotDate()).thenReturn(slotDate);
        lenient().when(timeSlot.getStartTime()).thenReturn(startTime);
        lenient().when(appointment.getFinalPrice()).thenReturn(finalPrice);

        return appointment;
    }
}
