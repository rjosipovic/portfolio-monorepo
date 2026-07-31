package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @Scheduled(fixedRate = 1_800_000) // every 30 min
    @SchedulerLock(name = "tutorNudgeJob", lockAtLeastFor = "5min", lockAtMostFor = "25m")
    public void nudgeTutor() {
        var timezone = ZoneId.of(brandProperties.getTimezone());
        var now = LocalTime.now(timezone);
        var today = LocalDate.now(timezone);
        var nudgeAfter = now.minus(schedulingProperties.getNudgeDelay());

        var unclosedAppointments = appointmentRepository.findUnclosedPastAppointments(UNCLOSED_STATES, today, nudgeAfter);

        log.info("TutorNudgeJob: found {} unclosed appointment past nudge delay", unclosedAppointments.size());

        unclosedAppointments.forEach(tutorNudgeHandler::handle);

        log.info("TutorNudgeJob: completed");
    }
}
