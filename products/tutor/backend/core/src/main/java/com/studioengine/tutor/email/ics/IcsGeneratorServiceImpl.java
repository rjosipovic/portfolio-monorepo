package com.studioengine.tutor.email.ics;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IcsGeneratorServiceImpl implements IcsGeneratorService {

    private final BrandProperties brandProperties;

    @Override
    public byte[] generateIcsFile(Appointment appointment) {
        try {
            var slot = appointment.getTimeSlot();
            var timezone = ZoneId.of(brandProperties.getTimezone());

            var start = ZonedDateTime.of(LocalDateTime.of(slot.getSlotDate(), slot.getStartTime()), timezone);
            var end = ZonedDateTime.of(LocalDateTime.of(slot.getSlotDate(), slot.getEndTime()), timezone);

            var event = new VEvent(start, end, appointment.getServiceCategory().getName());
            event.add(new Description(buildDescription(appointment)));
            event.add(new Location(brandProperties.getName()));
            event.add(new Uid(appointment.getId().toString()));

            var calendar = new Calendar();
            calendar.add(new ProdId("-//" + brandProperties.getName() + "//Tutor/HR"));
            calendar.add(new Version("2.0", null));
            calendar.add(new CalScale("GREGORIAN"));
            calendar.add(event);

            return calendar.toString().getBytes();

        } catch (Exception ex) {
            log.error("Failed to generate ICS file for appointment {}: {}", appointment.getId(), ex.getMessage());
            throw new RuntimeException("ICS generation failed", ex);
        }
    }

    private String buildDescription(Appointment appointment) {
        return "%s - %s\nStudent: %s".formatted(
                brandProperties.getName(),
                appointment.getServiceCategory().getName(),
                appointment.getStudent().getName()
        );
    }
}
