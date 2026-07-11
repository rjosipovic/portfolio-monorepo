package com.studioengine.tutor.booking;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentOrigin;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.ServiceCategoryRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DirectBookingServiceImpl implements DirectBookingService {

    private static final Set<TimeSlotState> ALLOWED_SLOT_STATES = Set.of(TimeSlotState.DRAFT, TimeSlotState.AVAILABLE);

    private final TimeSlotRepository timeSlotRepository;
    private final StudentRepository studentRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotStateMachine timeSlotStateMachine;

    @Override
    @Transactional
    public DirectBooking book(DirectBookingCommand command) {
        var slotId = command.getTimeSlotId();
        var studentId = command.getStudentId();
        var serviceCategoryId = command.getServiceCategoryId();

        var slot = findTimeSlot(slotId);
        verifyTimeSlotInAllowedState(slot);
        var student = findStudent(studentId);
        var category = findServiceCategory(serviceCategoryId);

        var appointment = Appointment.create(
                slot,
                category,
                student,
                AppointmentState.PRE_BOOKED,
                category.getPrice(),
                category.getPrice(),
                AppointmentOrigin.DASHBOARD_DIRECT,
                null
        );
        appointmentRepository.save(appointment);

        timeSlotStateMachine.transition(slot, TimeSlotState.PRE_BOOKED, "TUTOR");
        timeSlotRepository.save(slot);

        return DirectBooking.builder()
                .serviceCategoryId(category.getId())
                .serviceCategoryName(category.getName())
                .appointmentId(appointment.getId())
                .studentId(student.getId())
                .studentName(student.getName())
                .studentEmail(student.getEmail())
                .studentPhone(student.getPhone())
                .state(appointment.getState())
                .timeSlotId(slot.getId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .build();
    }

    private TimeSlot findTimeSlot(UUID id) {
        return timeSlotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found: " + id));
    }

    private void verifyTimeSlotInAllowedState(TimeSlot slot) {
        if (!ALLOWED_SLOT_STATES.contains(slot.getState())) {
            throw new InvalidStateTransitionException("Slot %s is in state %s, expected %s".formatted(slot.getId(), slot.getState(), ALLOWED_SLOT_STATES));
        }
    }

    private Student findStudent(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    private ServiceCategory findServiceCategory(UUID id) {
        return serviceCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ServiceCategory not found: " + id));
    }
}
