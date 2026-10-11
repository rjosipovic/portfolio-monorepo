package com.studioengine.tutor.email;

import com.studioengine.tutor.dataaccess.entities.Appointment;

import java.math.BigDecimal;
import java.util.UUID;

public interface EmailService {

    void sendConfirmationEmail(Appointment appointment, String manageLink);

    void sendPendingPaymentEmail(Appointment appointment, String manageLink);

    void sendOverdueNotificationToTutor(Appointment appointment);

    void sendOverdueNotificationToStudent(Appointment appointment);

    void sendReminder(Appointment appointment);

    void sendNudge(Appointment appointment);

    void sendFollowUp(Appointment appointment);

    void sendCancellationNotification(Appointment appointment, String reason);

    void sendOtpEmail(String email, String otp);

    void sendRescheduleNotification(Appointment originalAppointment, Appointment newAppointment);

    void sendOrphanPaymentAlert(UUID appointmentId, String paymentIntentId, BigDecimal amount);
}
