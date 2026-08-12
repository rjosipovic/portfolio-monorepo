package com.studioengine.tutor.email.ics;

import com.studioengine.tutor.dataaccess.entities.Appointment;

public interface IcsGeneratorService {

    byte[] generateIcsFile(Appointment appointment);
}
