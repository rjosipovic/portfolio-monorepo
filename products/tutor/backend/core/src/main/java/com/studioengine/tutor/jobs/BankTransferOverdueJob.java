package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankTransferOverdueJob {

    private final SchedulingProperties schedulingProperties;
    private final AppointmentRepository appointmentRepository;
    private final OverduePaymentHandler overduePaymentHandler;
    private final Clock clock;
    private final BrandProperties brandProperties;

    @Scheduled(fixedRate = 900_000)
    @SchedulerLock(name = "bankTransferOverdueJob", lockAtLeastFor = "PT5M", lockAtMostFor = "PT14M")
    public void notifyOverduePayments() {

        var cutoff = OffsetDateTime.now(clock).minus(schedulingProperties.getPaymentOverdueThreshold());
        var now = LocalDateTime.now(clock.withZone(ZoneId.of(brandProperties.getTimezone())));
        var overdueAppointmentIds = appointmentRepository.findOverduePendingPayments(cutoff, now.toLocalDate(), now.toLocalTime()).stream()
                .map(Appointment::getId)
                .toList();

        log.info("BankTransferOverdueJob: found {} overdue pending payments", overdueAppointmentIds.size());

        overdueAppointmentIds.forEach(overduePaymentHandler::handle);

        log.info("BankTransferOverdueJob: completed");
    }
}
