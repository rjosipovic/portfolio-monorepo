package com.studioengine.tutor.email;

import com.studioengine.tutor.dataaccess.entities.Appointment;

public interface EmailService {

    void sendConfirmationEmail(Appointment appointment);

    void sendPendingPaymentEmail(Appointment appointment);

    void sendOverdueNotificationToTutor(Appointment appointment);

    void sendOverdueNotificationToStudent(Appointment appointment);

    void sendReminder(Appointment appointment);

    void sendNudge(Appointment appointment);

    void sendFollowUp(Appointment appointment);

    void sendCancellationNotification(Appointment appointment, String reason);

    void sendOtpEmail(String email, String otp);
}
