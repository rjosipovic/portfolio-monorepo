package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReminderJob {

    private static final Set<AppointmentState> REMINDER_STATES = Set.of(
            AppointmentState.PAID,
            AppointmentState.CONFIRMED,
            AppointmentState.PRE_BOOKED
    );

    private final BrandProperties brandProperties;
    private final AppointmentRepository appointmentRepository;
    private final DailyReminderHandler dailyReminderHandler;

    @Scheduled(cron = "0 0 0 * * *", zone = "${app.brand.timezone}")
    @SchedulerLock(name = "dailyReminderJob", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    public void sendReminders() {
        var timezone = ZoneId.of(brandProperties.getTimezone());
        var today = LocalDate.now(timezone);

        var appointments = appointmentRepository.findByStatesAndSlotDate(REMINDER_STATES, today);

        log.info("DailyReminderJob: found {} appointments for today ({})", appointments.size(), today);

        appointments.forEach(dailyReminderHandler::handle);

        log.info("DailyReminderJob: completed");
    }
}
