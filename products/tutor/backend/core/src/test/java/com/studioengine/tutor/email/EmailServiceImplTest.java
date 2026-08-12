package com.studioengine.tutor.email;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.InstanceProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.email.ics.IcsGeneratorService;
import com.studioengine.tutor.email.pdf.PdfGeneratorService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private BrandProperties brandProperties;
    @Mock
    private InstanceProperties instanceProperties;
    @Mock
    private PdfGeneratorService pdfGeneratorService;
    @Mock
    private IcsGeneratorService icsGeneratorService;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        when(brandProperties.getName()).thenReturn("Math Studio");
        lenient().when(brandProperties.getCurrency()).thenReturn("EUR");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    @Test
    void shouldSendConfirmationWithIcsAndPdf() {
        // given
        var appointment = mockAppointment(BigDecimal.TEN);
        when(icsGeneratorService.generateIcsFile(appointment)).thenReturn("ics-content".getBytes());
        when(pdfGeneratorService.generateInvoicePdf(appointment)).thenReturn("pdf-content".getBytes());

        // when
        emailService.sendConfirmationEmail(appointment);

        // then
        verify(icsGeneratorService).generateIcsFile(appointment);
        verify(pdfGeneratorService).generateInvoicePdf(appointment);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void shouldSendConfirmationWithoutPdfWhenPriceIsZero() {
        // given
        var appointment = mockAppointment(BigDecimal.ZERO);
        when(icsGeneratorService.generateIcsFile(appointment)).thenReturn("ics-content".getBytes());

        // when
        emailService.sendConfirmationEmail(appointment);

        // then
        verify(icsGeneratorService).generateIcsFile(appointment);
        verify(pdfGeneratorService, never()).generateInvoicePdf(any());
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void shouldNotThrowWhenMailSenderFails() {
        // given
        var appointment = mockAppointment(BigDecimal.TEN);
        when(icsGeneratorService.generateIcsFile(appointment)).thenReturn("ics-content".getBytes());
        when(pdfGeneratorService.generateInvoicePdf(appointment)).thenReturn("pdf-content".getBytes());
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // when / then — no exception thrown
        assertDoesNotThrow(() -> emailService.sendConfirmationEmail(appointment));
    }

    private Appointment mockAppointment(BigDecimal price) {
        var appointment = mock(Appointment.class);
        var student = mock(Student.class);
        var category = mock(ServiceCategory.class);
        var slot = mock(TimeSlot.class);

        when(appointment.getStudent()).thenReturn(student);
        when(appointment.getServiceCategory()).thenReturn(category);
        when(appointment.getTimeSlot()).thenReturn(slot);
        when(appointment.getFinalPrice()).thenReturn(price);
        when(student.getEmail()).thenReturn("student@test.com");
        when(student.getName()).thenReturn("Marko");
        when(category.getName()).thenReturn("Math");
        when(slot.getSlotDate()).thenReturn(LocalDate.of(2026, 8, 10));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));

        return appointment;
    }
}