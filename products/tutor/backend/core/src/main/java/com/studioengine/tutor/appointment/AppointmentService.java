package com.studioengine.tutor.appointment;

public interface AppointmentService {

    ClosedAppointment close(CloseAppointmentCommand command);
    CanceledAppointment cancel(CancelAppointmentCommand command);
}
