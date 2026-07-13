package com.studioengine.tutor.booking;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DirectBookingServiceMapper {

    @Mapping(source = "appointment.id", target = "appointmentId")
    @Mapping(source = "appointment.state", target = "state")
    @Mapping(source = "slot.id", target = "timeSlotId")
    @Mapping(source = "slot.slotDate", target = "slotDate")
    @Mapping(source = "slot.startTime", target = "startTime")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "student.email", target = "studentEmail")
    @Mapping(source = "student.phone", target = "studentPhone")
    @Mapping(source = "category.id", target = "serviceCategoryId")
    @Mapping(source = "category.name", target = "serviceCategoryName")
    DirectBooking toDirectBooking(Appointment appointment, TimeSlot slot, Student student, ServiceCategory category);
}
