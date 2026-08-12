package com.studioengine.tutor.email.ics;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IcsGeneratorServiceImplTest {

    private static final UUID appointmentId = UUID.randomUUID();
    private static final String studentName = "Marko Markić";
    private static final String serviceCategoryName = "Pripreme za maturu";
    private static final LocalDate date = LocalDate.of(2026, 6, 22);
    private static final LocalTime startTime = LocalTime.of(11, 0);
    private static final LocalTime endTime = LocalTime.of(12, 0);
    private static final String brandName = "S matamatikom kroz život";
    private static final String timezone = "Europe/Zagreb";

    @Mock
    private BrandProperties brandProperties;

    @InjectMocks
    private IcsGeneratorServiceImpl icsGeneratorService;

    @Test
    void shouldGenerateIcsFile() {
        // given
        var appointment = createAppointment();

        // when
        var icsBytes = icsGeneratorService.generateIcsFile(appointment);

        // then
        assertThat(icsBytes).isNotNull();
        var content = new String(icsBytes);
        assertThat(content).contains("BEGIN:VCALENDAR");
        assertThat(content).contains("BEGIN:VEVENT");
        assertThat(content).contains(serviceCategoryName);
        assertThat(content).contains(studentName);
        assertThat(content).contains("END:VCALENDAR");
    }

    private Appointment createAppointment() {
        var student = mock(Student.class);
        var serviceCategory = mock(ServiceCategory.class);
        var timeSlot = mock(TimeSlot.class);
        var appointment = mock(Appointment.class);

        when(appointment.getId()).thenReturn(appointmentId);
        when(appointment.getStudent()).thenReturn(student);
        when(appointment.getServiceCategory()).thenReturn(serviceCategory);
        when(appointment.getTimeSlot()).thenReturn(timeSlot);
        when(student.getName()).thenReturn(studentName);
        when(serviceCategory.getName()).thenReturn(serviceCategoryName);
        when(timeSlot.getSlotDate()).thenReturn(date);
        when(timeSlot.getStartTime()).thenReturn(startTime);
        when(timeSlot.getEndTime()).thenReturn(endTime);
        when(brandProperties.getName()).thenReturn(brandName);
        when(brandProperties.getTimezone()).thenReturn(timezone);

        return appointment;
    }
}