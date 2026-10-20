package com.studioengine.tutor.selfservice;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.InstanceProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.CancellationToken;
import com.studioengine.tutor.dataaccess.repositories.CancellationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final CancellationTokenRepository cancellationTokenRepository;
    private final InstanceProperties instanceProperties;
    private final BrandProperties brandProperties;

    public String generateManageLink(Appointment appointment) {
        var zone = ZoneId.of(brandProperties.getTimezone());
        var slot = appointment.getTimeSlot();
        var expiresAt = slot.getSlotDate()
                .atTime(slot.getEndTime())
                .atZone(zone)
                .toOffsetDateTime();

        var token = CancellationToken.createManagement(appointment, expiresAt);
        cancellationTokenRepository.save(token);
        return "%s/manage.html?token=%s".formatted(instanceProperties.getBaseUrl(), token.getToken());
    }
}
