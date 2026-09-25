package com.studioengine.tutor.selfservice;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.InstanceProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.CancellationToken;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.TokenType;
import com.studioengine.tutor.dataaccess.repositories.CancellationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private CancellationTokenRepository cancellationTokenRepository;
    @Mock
    private InstanceProperties instanceProperties;
    @Mock
    private BrandProperties brandProperties;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void shouldGenerateManageLinkAndPersistToken() {
        // given
        var appointment = mock(Appointment.class);
        var slot = mock(TimeSlot.class);
        var slotDate = LocalDate.of(2026, 8, 20);
        var slotEnd = LocalTime.of(11, 0);

        when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
        when(appointment.getTimeSlot()).thenReturn(slot);
        when(slot.getSlotDate()).thenReturn(slotDate);
        when(slot.getEndTime()).thenReturn(slotEnd);
        when(instanceProperties.getBaseUrl()).thenReturn("http://localhost:8080");

        // when
        var link = tokenService.generateManageLink(appointment);

        // then
        verify(brandProperties).getTimezone();
        var captor = ArgumentCaptor.forClass(CancellationToken.class);
        verify(cancellationTokenRepository).save(captor.capture());
        var savedToken = captor.getValue();

        assertThat(savedToken.getTokenType()).isEqualTo(TokenType.APPOINTMENT_MANAGEMENT);
        assertThat(savedToken.getAppointment()).isEqualTo(appointment);
        // expiry is set to the slot end date-time
        var expectedExpiry = slotDate.atTime(slotEnd).atZone(ZoneId.of("Europe/Zagreb")).toOffsetDateTime();
        assertThat(savedToken.getExpiresAt().toInstant()).isEqualTo(expectedExpiry.toInstant());

        assertThat(link).isEqualTo("http://localhost:8080/manage.html?token=%s".formatted(savedToken.getToken()));
    }
}
