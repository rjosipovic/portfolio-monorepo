package com.studioengine.tutor.student;

import com.studioengine.tutor.dataaccess.entities.PaymentRecord;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.StudentNote;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.PaymentRecordRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentNoteRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentRepository;
import com.studioengine.tutor.errors.exceptions.EmailAlreadyInUseException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final StudentNoteRepository studentNoteRepository;
    private final StudentServiceMapper studentServiceMapper;

    @Override
    @Transactional
    public StudentProfile create(CreateStudentCommand command) {
        var name = command.getName();
        var email = command.getEmail();
        var phone = command.getPhone();

        verifyEmailUnique(email, null);

        var student = Student.create(name, email, phone);
        studentRepository.save(student);

        return studentServiceMapper.toProfile(student, emptyMetrics());
    }

    @Override
    @Transactional
    public StudentProfile update(UpdateStudentCommand command) {
        var studentId = command.getStudentId();
        var name = command.getName();
        var email = command.getEmail();
        var phone = command.getPhone();

        var student = findStudent(studentId);
        verifyEmailUnique(email, studentId);

        student.updateDetails(name, email, phone);
        var saved = studentRepository.save(student);
        return studentServiceMapper.toProfile(saved, calculateMetrics(student.getId()));
    }

    @Override
    public List<StudentSearchResult> search(String query) {
        var students = (Objects.isNull(query) || query.isBlank())
                ? studentRepository.findAll()
                : studentRepository.searchByNameOrEmail(query.trim().toLowerCase());

        return students.stream()
                .map(studentServiceMapper::toSearchResult)
                .toList();
    }

    @Override
    public StudentProfile getProfile(UUID studentId) {
        var student = findStudent(studentId);
        var metrics = calculateMetrics(studentId);
        return studentServiceMapper.toProfile(student, metrics);
    }

    @Override
    public List<AppointmentHistory> getAppointmentHistory(UUID studentId) {
        findStudent(studentId); // verify student exists

        return appointmentRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(studentServiceMapper::toAppointmentHistory)
                .toList();
    }

    @Override
    public NoteEntry addNote(AddNoteCommand command) {
        var studentId = command.getStudentId();
        var noteContent = command.getContent();

        var student = findStudent(studentId);
        var studentNote = StudentNote.create(student, noteContent);
        studentNoteRepository.save(studentNote);

        return studentServiceMapper.toNoteEntry(studentNote);
    }

    @Override
    public NoteEntry updateNote(UpdateNoteCommand command) {
        var studentId = command.getStudentId();
        var noteId = command.getNoteId();
        var noteContent = command.getContent();

        findStudent(studentId); // verify student exists

        var studentNote = findNote(noteId);
        validateNoteOwnership(studentNote, studentId);
        studentNote.updateContent(noteContent);

        studentNoteRepository.save(studentNote);

        return studentServiceMapper.toNoteEntry(studentNote);
    }

    @Override
    public List<NoteEntry> getNotes(UUID studentId) {
        findStudent(studentId); // verify student exists

        return studentNoteRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(studentServiceMapper::toNoteEntry)
                .toList();
    }

    // --- Private helpers ---

    private Student findStudent(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found: %s".formatted(id)));
    }

    private StudentNote findNote(UUID id) {
        return studentNoteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Note not found: %s".formatted(id)));
    }

    private void verifyEmailUnique(String email, UUID excludeStudentId) {
        studentRepository.findByEmail(email).ifPresent(student -> {
            if (!student.getId().equals(excludeStudentId)) {
                throw new EmailAlreadyInUseException("Email already in use: %s".formatted(email));
            }
        });
    }

    private static void validateNoteOwnership(StudentNote studentNote, UUID studentId) {
        if (!studentNote.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Note not found: %s".formatted(studentNote.getId()));
        }
    }

    private StudentProfile.Metrics emptyMetrics() {
        return StudentProfile.Metrics.builder()
                .totalLessonsCompleted(0)
                .totalRevenue(BigDecimal.ZERO)
                .lastLessonDate(null)
                .build();
    }

    private StudentProfile.Metrics calculateMetrics(UUID studentId) {
        var appointments = appointmentRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

        var completedCount = (int) appointments.stream()
                .filter(a -> a.getState() == AppointmentState.COMPLETED)
                .count();
        var totalRevenue = appointments.stream()
                .filter(a -> a.getState() == AppointmentState.COMPLETED)
                .map(a -> paymentRecordRepository.findByAppointmentId(a.getId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var lastLessonDate = appointments.stream()
                .filter(a -> a.getState() == AppointmentState.COMPLETED)
                .findFirst()
                .map(a -> a.getTimeSlot().getSlotDate())
                .orElse(null);

        return StudentProfile.Metrics.builder()
                .totalLessonsCompleted(completedCount)
                .totalRevenue(totalRevenue)
                .lastLessonDate(lastLessonDate)
                .build();
    }
}
