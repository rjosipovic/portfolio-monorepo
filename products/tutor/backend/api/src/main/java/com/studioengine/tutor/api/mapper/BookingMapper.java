package com.studioengine.tutor.api.mapper;

import com.studioengine.tutor.api.dto.booking.DirectBookingRequest;
import com.studioengine.tutor.api.dto.booking.DirectBookingResponse;
import com.studioengine.tutor.api.dto.summary.AppointmentSummary;
import com.studioengine.tutor.api.dto.summary.ServiceCategorySummary;
import com.studioengine.tutor.api.dto.summary.StudentSummary;
import com.studioengine.tutor.api.dto.summary.TimeSlotSummary;
import com.studioengine.tutor.booking.DirectBooking;
import com.studioengine.tutor.booking.DirectBookingCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    DirectBookingCommand toCommand(DirectBookingRequest request);

    @Mapping(target = "appointment", expression = "java(toAppointmentSummary(booking))")
    @Mapping(target = "timeSlot", expression = "java(toTimeSlotSummary(booking))")
    @Mapping(target = "student", expression = "java(toStudentSummary(booking))")
    @Mapping(target = "serviceCategory", expression = "java(toServiceCategorySummary(booking))")
    DirectBookingResponse toResponse(DirectBooking booking);

    default AppointmentSummary toAppointmentSummary(DirectBooking booking) {
        return AppointmentSummary.builder()
                .id(booking.getAppointmentId())
                .state(booking.getState().name())
                .build();
    }

    default TimeSlotSummary toTimeSlotSummary(DirectBooking booking) {
        return TimeSlotSummary.builder()
                .id(booking.getTimeSlotId())
                .date(booking.getSlotDate())
                .startTime(booking.getStartTime())
                .build();
    }

    default StudentSummary toStudentSummary(DirectBooking booking) {
        return StudentSummary.builder()
                .id(booking.getStudentId())
                .name(booking.getStudentName())
                .email(booking.getStudentEmail())
                .phone(booking.getStudentPhone())
                .build();
    }

    default ServiceCategorySummary toServiceCategorySummary(DirectBooking booking) {
        return ServiceCategorySummary.builder()
                .id(booking.getServiceCategoryId())
                .name(booking.getServiceCategoryName())
                .build();
    }
}
