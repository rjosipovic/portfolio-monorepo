package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorNudgeJobTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BrandProperties brandProperties;
    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private TutorNudgeHandler tutorNudgeHandler;

    @InjectMocks
    private TutorNudgeJob tutorNudgeJob;

    @Test
    void shouldTriggerNudges() {
        // given
        var appointment1 = mock(Appointment.class);
        var appointment2 = mock(Appointment.class);
        when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
        when(schedulingProperties.getNudgeDelay()).thenReturn(Duration.ofHours(2));
        when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of(appointment1, appointment2));

        // when
        tutorNudgeJob.nudgeTutor();

        // then
        verify(appointmentRepository).findUnclosedPastAppointments(any(), any(), any());
        verify(tutorNudgeHandler, times(2)).handle(argThat(a -> a.equals(appointment1) || a.equals(appointment2)));
    }

    @Test
    void shouldNotTriggerWhenNoUnclosedAppointments() {
        // given
        when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
        when(schedulingProperties.getNudgeDelay()).thenReturn(Duration.ofHours(2));
        when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());

        // when
        tutorNudgeJob.nudgeTutor();

        // then
        verify(tutorNudgeHandler, never()).handle(any());
    }
}
