package com.studioengine.tutor.email.pdf;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.PaymentProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfGeneratorServiceImplTest {

    @Mock
    private BrandProperties brandProperties;

    @Mock
    private PaymentProperties paymentProperties;

    @InjectMocks
    private PdfGeneratorServiceImpl pdfGeneratorService;

    @Nested
    class PdfGenerationTests {
        private static final UUID appointmentId = UUID.randomUUID();
        private static final String studentName = "Marko Markić";
        private static final String serviceCategoryName = "Pripreme za maturu";
        private static final LocalDate date = LocalDate.of(2026, 6, 22);
        private static final LocalTime startTime = LocalTime.of(11, 0);
        private static final BigDecimal price = BigDecimal.valueOf(25);
        private static final String bankIban = "HR1210010051863000160";
        private static final String bankModel = "HR00";
        private static final String bankRecipient = "Tutor Tutić";
        private static final String brandName = "S matamatikom kroz život";
        private static final String currency = "EUR";

        @Test
        @Disabled
        void shouldGeneratePdfAndWriteFile() throws IOException {
            // given
            var appointment = createAppointment();

            // when
            var pdfByteArray = pdfGeneratorService.generateInvoicePdf(appointment);

            // then
            Files.write(Path.of("/tmp/invoice-" + appointment.getId() + ".pdf"), pdfByteArray);
        }

        @Test
        void shouldGenerateInvoicePdf() {
            // given
            var appointment = createAppointment();

            // when
            var pdfByteArray = pdfGeneratorService.generateInvoicePdf(appointment);

            // then
            assertThat(pdfByteArray).isNotNull();
            assertThat(pdfByteArray.length).isGreaterThan(0);

            try (var document = Loader.loadPDF(pdfByteArray)) {
                assertThat(document.getNumberOfPages()).isEqualTo(1);

                var textStripper = new PDFTextStripper();
                var text = textStripper.getText(document);
                assertThat(text).contains(brandName);
                assertThat(text).contains(studentName);
                assertThat(text).contains(serviceCategoryName);
                assertThat(text).contains(bankIban);
                assertThat(text).contains(bankModel);
                assertThat(text).contains(bankRecipient);
                assertThat(text).contains(currency);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private Appointment createAppointment() {
            var student = mock(Student.class);
            var serviceCategory = mock(ServiceCategory.class);
            var timeSlot = mock(TimeSlot.class);
            var appointment = mock(Appointment.class);

            lenient().when(appointment.getId()).thenReturn(appointmentId);
            when(appointment.getStudent()).thenReturn(student);
            when(appointment.getServiceCategory()).thenReturn(serviceCategory);
            when(appointment.getTimeSlot()).thenReturn(timeSlot);
            when(appointment.getFinalPrice()).thenReturn(price);
            when(student.getName()).thenReturn(studentName);
            when(serviceCategory.getName()).thenReturn(serviceCategoryName);
            when(timeSlot.getSlotDate()).thenReturn(date);
            when(timeSlot.getStartTime()).thenReturn(startTime);
            when(paymentProperties.getBankIban()).thenReturn(bankIban);
            when(paymentProperties.getBankModel()).thenReturn(bankModel);
            when(paymentProperties.getBankRecipientName()).thenReturn(bankRecipient);
            when(brandProperties.getName()).thenReturn(brandName);
            when(brandProperties.getCurrency()).thenReturn(currency);

            return appointment;
        }
    }
}