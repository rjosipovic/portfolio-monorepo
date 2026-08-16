package com.studioengine.tutor.email;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "app.email.enabled", havingValue = "false")
@Component
@Slf4j
public class NoOpEmailService implements EmailService {

    @Override
    public void sendConfirmationEmail(Appointment appointment) {
        log.info("[NO-OP] Confirmation email for appointment {}", appointment.getId());
    }

    @Override
    public void sendPendingPaymentEmail(Appointment appointment) {
        log.info("[NO-OP] Pending payment email for appointment {}", appointment.getId());
    }

    @Override
    public void sendOverdueNotificationToTutor(Appointment appointment) {
        log.info("[NO-OP] Overdue notification to tutor for appointment {}", appointment.getId());
    }

    @Override
    public void sendOverdueNotificationToStudent(Appointment appointment) {
        log.info("[NO-OP] Overdue notification to student for appointment {}", appointment.getId());
    }

    @Override
    public void sendReminder(Appointment appointment) {
        log.info("[NO-OP] Reminder email for appointment {}", appointment.getId());
    }

    @Override
    public void sendNudge(Appointment appointment) {
        log.info("[NO-OP] Nudge email for appointment {}", appointment.getId());
    }

    @Override
    public void sendFollowUp(Appointment appointment) {
        log.info("[NO-OP] Follow-up email for appointment {}", appointment.getId());
    }

    @Override
    public void sendCancellationNotification(Appointment appointment, String reason) {
        log.info("[NO-OP] Cancellation notification for appointment {}, reason: {}", appointment.getId(), reason);
    }

    @Override
    public void sendOtpEmail(String email, String otp) {
        log.info("[NO-OP] Sending OTP {} to {}", otp, email);
    }
}
