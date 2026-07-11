package com.studioengine.tutor.student;

import java.util.List;
import java.util.UUID;

public interface StudentService {

    StudentProfile create(CreateStudentCommand command);

    StudentProfile update(UpdateStudentCommand command);

    List<StudentSearchResult> search(String query);

    StudentProfile getProfile(UUID studentId);

    List<AppointmentHistory> getAppointmentHistory(UUID studentId);

    NoteEntry addNote(AddNoteCommand command);

    NoteEntry updateNote(UpdateNoteCommand command);

    List<NoteEntry> getNotes(UUID studentId);
}
