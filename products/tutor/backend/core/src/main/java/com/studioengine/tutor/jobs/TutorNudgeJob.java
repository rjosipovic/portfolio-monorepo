package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TutorNudgeJob {

    private static final Set<AppointmentState> UNCLOSED_STATES = Set.of(
            AppointmentState.PAID,
            AppointmentState.CONFIRMED,
            AppointmentState.PRE_BOOKED
    );

    private final AppointmentRepository appointmentRepository;
    private final BrandProperties brandProperties;
    private final SchedulingProperties schedulingProperties;
    private final TutorNudgeHandler tutorNudgeHandler;
    private final Clock clock;

    @Scheduled(fixedRate = 1_800_000) // every 30 min
    @SchedulerLock(name = "tutorNudgeJob", lockAtLeastFor = "PT5M", lockAtMostFor = "PT25M")
    public void nudgeTutor() {
        var timezone = ZoneId.of(brandProperties.getTimezone());
        var cutoff = LocalDateTime.now(clock.withZone(timezone)).minus(schedulingProperties.getNudgeDelay());
        var cutoffDate = cutoff.toLocalDate();
        var cutoffTime = cutoff.toLocalTime();

        var unclosedAppointmentIds = appointmentRepository
                .findUnclosedPastAppointments(UNCLOSED_STATES, cutoffDate, cutoffTime).stream()
                .map(Appointment::getId)
                .toList();

        log.info("TutorNudgeJob: found {} unclosed appointment past nudge delay", unclosedAppointmentIds.size());

        unclosedAppointmentIds.forEach(tutorNudgeHandler::handle);

        log.info("TutorNudgeJob: completed");
    }
}
