package com.studioengine.tutor.landing;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LandingServiceImpl implements LandingService {

    private static final Set<AppointmentState> CLOSABLE_STATES = Set.of(
            AppointmentState.PAID,
            AppointmentState.CONFIRMED,
            AppointmentState.PRE_BOOKED);

    private final AppointmentRepository appointmentRepository;
    private final BrandProperties brandProperties;

    @Override
    public DashboardOverview getLandingPageData() {

        var timezone = ZoneId.of(brandProperties.getTimezone());
        var today = LocalDate.now(timezone);
        var now = LocalTime.now(timezone);

        var awaitingClosure = getAwaitingClosureActionItems(today, now);
        var pendingPayments = getPendingPaymentsActionItems();
        var todayUpcomingActionItems = getTodayUpcomingActionItems(today, now);
        return DashboardOverview.builder()
                .awaitingClosure(awaitingClosure)
                .pendingPayments(pendingPayments)
                .todayUpcoming(todayUpcomingActionItems)
                .build();
    }

    private List<DashboardOverview.ActionItem> getAwaitingClosureActionItems(LocalDate today, LocalTime now) {
        var appointments = appointmentRepository.findUnclosedPastAppointments(CLOSABLE_STATES, today, now);
        return appointments.stream()
                .map(this::toActionItem)
                .toList();
    }

    private List<DashboardOverview.ActionItem> getPendingPaymentsActionItems() {
        var appointments = appointmentRepository.findByStateIn(Set.of(AppointmentState.PENDING_PAYMENT));
        return appointments.stream()
                .map(this::toActionItem)
                .toList();
    }

    private List<DashboardOverview.ActionItem> getTodayUpcomingActionItems(LocalDate today, LocalTime now) {
        var appointments = appointmentRepository.findByStatesAndSlotDate(CLOSABLE_STATES, today);
        return appointments.stream()
                .filter(appointment -> appointment.getTimeSlot().getStartTime().isAfter(now))
                .map(this::toActionItem)
                .toList();
    }

    private DashboardOverview.ActionItem toActionItem(Appointment appointment) {
        return DashboardOverview.ActionItem.builder()
                .appointmentId(appointment.getId())
                .studentName(appointment.getStudent().getName())
                .serviceCategory(appointment.getServiceCategory().getName())
                .date(appointment.getTimeSlot().getSlotDate())
                .startTime(appointment.getTimeSlot().getStartTime())
                .amount(appointment.getFinalPrice())
                .build();
    }
}
