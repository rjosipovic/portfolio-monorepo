package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationTimeoutJobTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private ExpiredTimeSlotHandler expiredTimeSlotHandler;

    @InjectMocks
    private ReservationTimeoutJob reservationTimeoutJob;

    @Test
    void shouldTriggerReleaseExpiredReservations() {
        // given
        var min15 = Duration.of(900, ChronoUnit.SECONDS);
        var ts1 = mock(TimeSlot.class);
        var ts2 = mock(TimeSlot.class);
        when(schedulingProperties.getReservationTimeout()).thenReturn(min15);
        when(timeSlotRepository.findExpiredReservations(any(OffsetDateTime.class))).thenReturn(List.of(ts1, ts2));

        // when
        reservationTimeoutJob.releaseExpiredReservations();

        // then
        verify(schedulingProperties).getReservationTimeout();

        var captor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(timeSlotRepository).findExpiredReservations(captor.capture());
        var cutoff = captor.getValue();
        assertThat(Duration.between(cutoff, OffsetDateTime.now())).isGreaterThanOrEqualTo(min15);

        verify(expiredTimeSlotHandler, times(2)).handle(argThat(slot -> slot.equals(ts1) || slot.equals(ts2)));
    }

    @Test
    void shouldNotTriggerReleaseExpiredReservationsWhenNoTimeSlots() {
        // given
        var min15 = Duration.of(900, ChronoUnit.SECONDS);
        when(schedulingProperties.getReservationTimeout()).thenReturn(min15);
        when(timeSlotRepository.findExpiredReservations(any(OffsetDateTime.class))).thenReturn(List.of());

        // when
        reservationTimeoutJob.releaseExpiredReservations();

        // then
        verify(expiredTimeSlotHandler, never()).handle(any());
    }
}