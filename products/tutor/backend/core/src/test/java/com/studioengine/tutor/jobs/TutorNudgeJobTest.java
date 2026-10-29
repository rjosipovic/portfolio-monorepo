package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorNudgeJobTest {

    private static final Set<AppointmentState> UNCLOSED_STATES = Set.of(
            AppointmentState.PAID,
            AppointmentState.CONFIRMED,
            AppointmentState.PRE_BOOKED
    );
    private static final String ZONE_ID = "Europe/Zagreb";

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BrandProperties brandProperties;
    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private TutorNudgeHandler tutorNudgeHandler;

    private Clock clock;
    private TutorNudgeJob tutorNudgeJob;

    private void initJob(LocalDateTime fixedNow) {
        var zone = ZoneId.of(ZONE_ID);
        clock = Clock.fixed(fixedNow.atZone(zone).toInstant(), zone);
        tutorNudgeJob = new TutorNudgeJob(appointmentRepository, brandProperties, schedulingProperties, tutorNudgeHandler, clock);
    }

    @Test
    void shouldTriggerNudges() {
        // given
        var yearNow = 2026;
        var monthNow = 9;
        var dayNow = 22;
        var hourNow = 12;
        var minNow = 0;
        var nudgeDelayHours = 2;
        var fixedNow = LocalDateTime.of(yearNow, monthNow, dayNow, hourNow, minNow);
        initJob(fixedNow);
        var cutoff = fixedNow.minus(Duration.ofHours(nudgeDelayHours));
        var todayFixture = cutoff.toLocalDate();
        var nudgeAfterFixture = cutoff.toLocalTime();
        var appointmentId1 = UUID.randomUUID();
        var appointmentId2 = UUID.randomUUID();
        var appointment1 = mock(Appointment.class);
        var appointment2 = mock(Appointment.class);
        var appointments = List.of(appointment1, appointment2);
        when(appointment1.getId()).thenReturn(appointmentId1);
        when(appointment2.getId()).thenReturn(appointmentId2);
        when(brandProperties.getTimezone()).thenReturn(ZONE_ID);
        when(schedulingProperties.getNudgeDelay()).thenReturn(Duration.ofHours(nudgeDelayHours));
        when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(appointments);

        // when
        tutorNudgeJob.nudgeTutor();

        // then
        var dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        var timeCaptor = ArgumentCaptor.forClass(LocalTime.class);
        verify(appointmentRepository).findUnclosedPastAppointments(any(), dateCaptor.capture(), timeCaptor.capture());

        assertThat(dateCaptor.getValue()).isEqualTo(todayFixture);
        assertThat(timeCaptor.getValue()).isEqualTo(nudgeAfterFixture);

        verify(tutorNudgeHandler, times(appointments.size())).handle(argThat(a -> a.equals(appointment1.getId()) || a.equals(appointment2.getId())));
    }

    @Test
    void shouldNotTriggerWhenNoUnclosedAppointments() {
        // given
        var yearNow = 2026;
        var monthNow = 9;
        var dayNow = 22;
        var hourNow = 12;
        var minNow = 0;
        var nudgeDelayHours = 2;
        var fixedNow = LocalDateTime.of(yearNow, monthNow, dayNow, hourNow, minNow);
        initJob(fixedNow);
        when(brandProperties.getTimezone()).thenReturn(ZONE_ID);
        when(schedulingProperties.getNudgeDelay()).thenReturn(Duration.ofHours(nudgeDelayHours));
        when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());

        // when
        tutorNudgeJob.nudgeTutor();

        // then
        verify(tutorNudgeHandler, never()).handle(any());
    }

    @Test
    void shouldComputeCutoffOnPreviousDayWhenRunJustAfterMidnight() {
        // given — job runs at 00:21 on 2026-10-28, nudge delay 2h
        // correct cutoff = 2026-10-28 00:21 - 2h = 2026-10-27 22:21
        var yearNow = 2026;
        var monthNow = 10;
        var dayNow = 28;
        var hourNow = 0;
        var minNow = 21;
        var nudgeDelayHours = 2;
        var fixedNow = LocalDateTime.of(yearNow, monthNow, dayNow, hourNow, minNow);
        initJob(fixedNow);
        var cutoff = fixedNow.minus(Duration.ofHours(nudgeDelayHours));
        var todayFixture = cutoff.toLocalDate();
        var nudgeAfterFixture = cutoff.toLocalTime();
        when(brandProperties.getTimezone()).thenReturn(ZONE_ID);
        when(schedulingProperties.getNudgeDelay()).thenReturn(Duration.ofHours(nudgeDelayHours));
        when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());

        // when
        tutorNudgeJob.nudgeTutor();

        // then — the date must have decremented with the time wrap
        var dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        var timeCaptor = ArgumentCaptor.forClass(LocalTime.class);
        verify(appointmentRepository).findUnclosedPastAppointments(any(), dateCaptor.capture(), timeCaptor.capture());

        assertThat(dateCaptor.getValue()).isEqualTo(todayFixture);
        assertThat(timeCaptor.getValue()).isEqualTo(nudgeAfterFixture);
    }

    @Test
    void shouldComputeCutoffSameDayWhenRunMidDay() {
        // given — job runs at 14:00 on 2026-10-28, nudge delay 2h → cutoff 2026-10-28 12:00 (no wrap)
        var yearNow = 2026;
        var monthNow = 10;
        var dayNow = 28;
        var hourNow = 14;
        var minNow = 0;
        var nudgeDelayHours = 2;
        var fixedNow = LocalDateTime.of(yearNow, monthNow, dayNow, hourNow, minNow);
        initJob(fixedNow);
        var cutoff = fixedNow.minus(Duration.ofHours(nudgeDelayHours));
        var todayFixture = cutoff.toLocalDate();
        var nudgeAfterFixture = cutoff.toLocalTime();
        when(brandProperties.getTimezone()).thenReturn(ZONE_ID);
        when(schedulingProperties.getNudgeDelay()).thenReturn(Duration.ofHours(nudgeDelayHours));
        when(appointmentRepository.findUnclosedPastAppointments(any(), any(), any())).thenReturn(List.of());

        // when
        tutorNudgeJob.nudgeTutor();

        // then
        var dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        var timeCaptor = ArgumentCaptor.forClass(LocalTime.class);
        verify(appointmentRepository).findUnclosedPastAppointments(any(), dateCaptor.capture(), timeCaptor.capture());

        assertThat(dateCaptor.getValue()).isEqualTo(todayFixture);
        assertThat(timeCaptor.getValue()).isEqualTo(nudgeAfterFixture);
    }
}
