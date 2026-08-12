package com.studioengine.tutor.email.pdf;

import com.studioengine.tutor.dataaccess.entities.Appointment;

public interface PdfGeneratorService {

    byte[] generateInvoicePdf(Appointment appointment);
}
