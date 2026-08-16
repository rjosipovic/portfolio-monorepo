package com.studioengine.tutor.email;

import com.studioengine.tutor.config.AuthProperties;
import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.InstanceProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.email.ics.IcsGeneratorService;
import com.studioengine.tutor.email.pdf.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true", matchIfMissing = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final JavaMailSender mailSender;
    private final BrandProperties brandProperties;
    private final InstanceProperties instanceProperties;
    private final PdfGeneratorService pdfGeneratorService;
    private final IcsGeneratorService icsGeneratorService;
    private final AuthProperties authProperties;

    @Override
    public void sendConfirmationEmail(Appointment appointment) {
        var student = appointment.getStudent();
        var serviceCategory = appointment.getServiceCategory();
        var slot = appointment.getTimeSlot();
        var subject = "%s - Potvrda termina".formatted(brandProperties.getName());
        var body = """
                <h2>Potvrda rezervacije</h2>
                <p>Poštovani/a %s,</p>
                <p>Vaš termin je potvrđen:</p>
                <ul>
                    <li><strong>Usluga:</strong> %s</li>
                    <li><strong>Datum:</strong> %s</li>
                    <li><strong>Vrijeme:</strong> %s</li>
                </ul>
                <p>U privitku se nalazi kalendarski poziv (.ics) koji možete dodati u kalendar.</p>
                <p>Srdačan pozdrav,<br/>%s</p>
                """.formatted(
                student.getName(),
                serviceCategory.getName(),
                slot.getSlotDate().format(DATE_FMT),
                slot.getStartTime().format(TIME_FMT),
                brandProperties.getName()
        );
        var icsFile = icsGeneratorService.generateIcsFile(appointment);
        byte[] pdfFile = null;
        if (appointment.getFinalPrice().signum() > 0) {
            pdfFile = pdfGeneratorService.generateInvoicePdf(appointment);
        }
        sendHtmlEmail(student.getEmail(), subject, body, icsFile, pdfFile);
    }

    @Override
    public void sendPendingPaymentEmail(Appointment appointment) {
        var student = appointment.getStudent();
        var slot = appointment.getTimeSlot();
        var serviceCategory = appointment.getServiceCategory();
        var subject = "%s - Upute za plaćanje".formatted(brandProperties.getName());
        // TODO: don't hardcode deadline values, use configuration
        var body = """
                <h2>Upute za plaćanje</h2>
                <p>Poštovani/a %s,</p>
                <p>Vaš termin je rezerviran. Molimo izvršite uplatu u roku od 48 sati.</p>
                <ul>
                    <li><strong>Usluga:</strong> %s</li>
                    <li><strong>Datum:</strong> %s</li>
                    <li><strong>Vrijeme:</strong> %s</li>
                    <li><strong>Iznos:</strong> %s %s</li>
                </ul>
                <p>U privitku se nalazi račun s HUB3 barkodom za plaćanje.</p>
                <p>Srdačan pozdrav, <br/>%s</p>
                """.formatted(
                student.getName(),
                serviceCategory.getName(),
                slot.getSlotDate().format(DATE_FMT),
                slot.getStartTime().format(TIME_FMT),
                appointment.getFinalPrice(),
                brandProperties.getCurrency(),
                brandProperties.getName()
        );
        var pdfFile = pdfGeneratorService.generateInvoicePdf(appointment);
        sendHtmlEmail(student.getEmail(), subject, body, null, pdfFile);
    }

    @Override
    public void sendOverdueNotificationToTutor(Appointment appointment) {
        var student = appointment.getStudent();
        var serviceCategory = appointment.getServiceCategory();
        var slot = appointment.getTimeSlot();
        var subject = "%s - Uplata nije potvrđena".formatted(brandProperties.getName());
        // TODO don't hardcode deadline values, use configuration
        var body = """
                <h2>Uplata nije potvrđena</h2>
                <p>Uplata za sljedeći termin nije potvrđena u roku od 48 sati:</p>
                <ul>
                    <li><strong>Student:</strong> %s</li>
                    <li><strong>Usluga:</strong> %s</li>
                    <li><strong>Datum:</strong> %s</li>
                </ul>
                <p>Molimo potvrdite uplatu ili otkažite termin</p>
                """.formatted(
                student.getName(),
                serviceCategory.getName(),
                slot.getSlotDate().format(DATE_FMT)
        );

        sendHtmlEmail(getTutorEmail(), subject, body, null, null);
    }

    @Override
    public void sendOverdueNotificationToStudent(Appointment appointment) {
        var student = appointment.getStudent();
        var slot = appointment.getTimeSlot();
        var subject = "%s - Uplata nije potvrđena".formatted(brandProperties.getName());
        var body = """
                <h2>Uplata nije potvrđena</h2>
                <p>Poštovani/a %s,</p>
                <p>vaša uplata za termin %s još nije potvrđena. Ako ste izvršili uplatu, kontaktirajte instruktora</p>
                <p>Srdačan pozdrav, <br/>%s</p>
                """.formatted(
                student.getName(),
                slot.getSlotDate().format(DATE_FMT),
                brandProperties.getName()
        );

        sendHtmlEmail(student.getEmail(), subject, body, null, null);
    }

    @Override
    public void sendReminder(Appointment appointment) {
        var student = appointment.getStudent();
        var serviceCategory = appointment.getServiceCategory();
        var slot = appointment.getTimeSlot();
        var subject = "%s - Podsjetnik za termin".formatted(brandProperties.getName());
        var body = """
                <h2>Podsjetnik za termin</h2>
                <p>Poštovani/a %s,</p>
                <p>Podsjećam Vas na termin danas:</p>
                <ul>
                    <li><strong>Usluga:</strong> %s</li>
                    <li><strong>Vrijeme:</strong> %s</li>
                </ul>
                <p>Vidimo se!<br/>%s</p>
                """.formatted(
                        student.getName(),
                serviceCategory.getName(),
                slot.getStartTime().format(TIME_FMT),
                brandProperties.getName()
        );

        sendHtmlEmail(student.getEmail(), subject, body, null, null);
    }

    @Override
    public void sendNudge(Appointment appointment) {
        var student = appointment.getStudent();
        var serviceCategory = appointment.getServiceCategory();
        var slot = appointment.getTimeSlot();
        var subject = "%s - Termin čeka zatvaranje".formatted(brandProperties.getName());
        var body = """
                <h2>Nezatvoreni termin</h2>
                <p>Sljedeći termin još nije zatvoren:</p>
                <ul>
                    <li><strong>Student:</strong> %s</li>
                    <li><strong>Usluga:</strong> %s</li>
                    <li><strong>Datum:</strong> %s</li>
                    <li><strong>Vrijeme:</strong> %s</li>
                </ul>
                <p>Molimo označite termin kao održan ili neostvaren</p>
                """.formatted(
                student.getName(),
                serviceCategory.getName(),
                slot.getSlotDate().format(DATE_FMT),
                slot.getStartTime().format(TIME_FMT)
        );

        sendHtmlEmail(getTutorEmail(), subject, body, null, null);
    }

    @Override
    public void sendFollowUp(Appointment appointment) {
        var student = appointment.getStudent();
        var subject = "%s - Hvala na posjetu".formatted(brandProperties.getName());
        var body = """
                <h2>Hvala!</h2>
                <p>Poštovani/a %s,</p>
                <p>Hvala što ste bili na terminu. Nadamo se da ste zadovoljni.</p>
                <p>Želite li rezervirati novi termin?</p>
                <p><a href=%s>Rezervirajte novi termin</a></p>
                <p>Srdačan pozdrav,<br/>%s</p>
                """.formatted(
                        student.getName(),
                instanceProperties.getBaseUrl(),
                brandProperties.getName()
        );

        sendHtmlEmail(student.getEmail(), subject, body, null, null);
    }

    @Override
    public void sendCancellationNotification(Appointment appointment, String reason) {
        var student = appointment.getStudent();
        var serviceCategory = appointment.getServiceCategory();
        var slot = appointment.getTimeSlot();
        var subject = "%s - Termin otkazan".formatted(brandProperties.getName());
        var body = """
                <h2>Termin otkazan</h2>
                <p>Poštovani/a %s</p>
                <p>Vaš termin je otkazan</p>
                <ul>
                    <li><strong>Usluga:</strong> %s</li>
                    <li><strong>Datum:</strong> %s</li>
                    <li><strong>Vrijeme:</strong> %s</li>
                    <li><strong>Razlog:</strong> %s</li>
                </ul>
                <p>Srdačan pozdrav, <br/>%s</p>
                """.formatted(
                        student.getName(),
                serviceCategory.getName(),
                slot.getSlotDate().format(DATE_FMT),
                slot.getStartTime().format(TIME_FMT),
                reason,
                brandProperties.getName()
        );

        sendHtmlEmail(student.getEmail(), subject, body, null, null);
    }

    @Override
    public void sendOtpEmail(String email, String otp) {
        var subject = "%s - Vaš kod za prijavu".formatted(brandProperties.getName());
        var body = """
                <h2>Prijava</h2>
                <p>Kod za prijavu: <strong>%s</strong></p>
                <p>Ovaj kod je validan 10 minuta</p>
                """.formatted(otp);

        sendHtmlEmail(email, subject, body, null, null);
    }

    // --- Private helpers ---
    private void sendHtmlEmail(String to, String subject, String htmlBody, byte[] icsAttachment, byte[] pdfAttachment) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom("noreply@%s".formatted(brandProperties.getName().toLowerCase().replace(" ", "")));

            if (Objects.nonNull(icsAttachment)) {
                helper.addAttachment("termin.ics", new ByteArrayResource(icsAttachment), "text/calendar");
            }

            if (Objects.nonNull(pdfAttachment)) {
                helper.addAttachment("racun.pdf", new ByteArrayResource(pdfAttachment), "application/pdf");
            }

            mailSender.send(message);
            log.info("Email sent to {} - subject: {}", to, subject);

        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
            // we don't throw - email failure should not roll back state transition
        }
    }

    private String getTutorEmail() {
        return authProperties.getInstructorEmail();
    }
}
