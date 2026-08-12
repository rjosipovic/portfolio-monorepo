package com.studioengine.tutor.email.pdf;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.pdf417.PDF417Writer;
import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.PaymentProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMAT_HEADER = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private static final DateTimeFormatter TIME_FORMAT_HEADER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT_REF_NUM = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT_REF_NUM = DateTimeFormatter.ofPattern("HHmm");

    private final PaymentProperties paymentProperties;
    private final BrandProperties brandProperties;

    @Override
    public byte[] generateInvoicePdf(Appointment appointment) {
        try (var document = new PDDocument()) {
            var page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (var contentStream = new PDPageContentStream(document, page)) {
                var font = PDType0Font.load(document, getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"));
                writeHeader(contentStream, appointment, font);
                writePaymentDetails(contentStream, appointment, font);
                writeHub3Barcode(document, contentStream, appointment);
            }

            var outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (Exception ex) {
            log.error("Failed to generate invoice PDF for appointment {}: {}", appointment.getId(), ex.getMessage());
            throw new RuntimeException("PDF generation failed", ex);
        }
    }

    private void writeHeader(PDPageContentStream contentStream, Appointment appointment, PDType0Font font) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, 16);
        contentStream.newLineAtOffset(50, 780);
        contentStream.showText(brandProperties.getName() + " - Račun");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("Student: " + appointment.getStudent().getName());
        contentStream.newLineAtOffset(0, -18);
        contentStream.showText("Usluga: " + appointment.getServiceCategory().getName());
        contentStream.newLineAtOffset(0, -18);
        contentStream.showText("Datum: " + appointment.getTimeSlot().getSlotDate().format(DATE_FORMAT_HEADER));
        contentStream.newLineAtOffset(0, -18);
        contentStream.showText("Vrijeme: " + appointment.getTimeSlot().getStartTime().format(TIME_FORMAT_HEADER));
        contentStream.endText();
    }

    private void writePaymentDetails(PDPageContentStream contentStream, Appointment appointment, PDType0Font font) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 650);
        contentStream.showText("Iznos: " + appointment.getFinalPrice() + " " + brandProperties.getCurrency());
        contentStream.newLineAtOffset(0, -18);
        contentStream.showText("IBAN: " + paymentProperties.getBankIban());
        contentStream.newLineAtOffset(0, -18);
        contentStream.showText("Model: " + paymentProperties.getBankModel());
        contentStream.newLineAtOffset(0, -18);
        contentStream.showText("Primatelj: " + paymentProperties.getBankRecipientName());
        contentStream.newLineAtOffset(0, -18);
        contentStream.showText("Poziv na broj: " + generateReferenceNumber(appointment));
        contentStream.endText();
    }

    private void writeHub3Barcode(PDDocument document, PDPageContentStream contentStream, Appointment appointment) throws Exception {
        var barcodePayload = buildHub3Payload(appointment);
        var barcodeImage = generateBarcode(barcodePayload, 452, 148);

        var imageBytes = toBytes(barcodeImage);
        var pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "hub3-barcode");

        contentStream.drawImage(pdImage, 50, 430, 300, 100);
    }

    private String buildHub3Payload(Appointment appointment) {
        return String.join(
                "\n",
                "HRVHUB30",
                brandProperties.getCurrency(),
                formatAmount(appointment.getFinalPrice()),
                appointment.getStudent().getName(),
                "",
                "",
                paymentProperties.getBankRecipientName(),
                "",
                "",
                paymentProperties.getBankIban(),
                paymentProperties.getBankModel(),
                generateReferenceNumber(appointment),
                "COST",
                appointment.getServiceCategory().getName()
        ) + "\n";
    }

    private String formatAmount(BigDecimal amount) {
        return "%015d".formatted((Long)amount.movePointRight(2).longValue());
    }

    private String generateReferenceNumber(Appointment appointment) {
        var date = appointment.getTimeSlot().getSlotDate();
        var time = appointment.getTimeSlot().getStartTime();
        return "%s-%s".formatted(
                date.format(DATE_FORMAT_REF_NUM),
                time.format(TIME_FORMAT_REF_NUM)
        );
    }

    private BufferedImage generateBarcode(String content, int width, int height) throws WriterException {
        var writer = new PDF417Writer();
        var hints = Map.of(EncodeHintType.CHARACTER_SET, "UTF-8");
        var bitMatrix = writer.encode(content, BarcodeFormat.PDF_417, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private byte[] toBytes(BufferedImage image) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}
