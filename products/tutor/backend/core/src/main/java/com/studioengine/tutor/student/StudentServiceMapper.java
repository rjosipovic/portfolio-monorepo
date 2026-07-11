package com.studioengine.tutor.student;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.StudentNote;
import org.mapstruct.Mapping;

public interface StudentServiceMapper {

    @Mapping(target = "metrics", source = "metrics")
    StudentProfile toProfile(Student student, StudentProfile.Metrics metrics);

    StudentSearchResult toSearchResult(Student student);

    NoteEntry toNoteEntry(StudentNote note);

    @Mapping(source = "id", target = "appointmentId")
    @Mapping(source = "timeSlot.slotDate", target = "date")
    @Mapping(source = "timeSlot.startTime", target = "startTime")
    @Mapping(source = "serviceCategory.name", target = "serviceCategoryName")
    AppointmentHistory toAppointmentHistory(Appointment appointment);
}
