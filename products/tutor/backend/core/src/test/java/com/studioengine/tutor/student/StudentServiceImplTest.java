package com.studioengine.tutor.student;


import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.PaymentRecord;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.StudentNote;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.PaymentRecordRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentNoteRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentRepository;
import com.studioengine.tutor.errors.exceptions.EmailAlreadyInUseException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;
    @Mock
    private StudentNoteRepository studentNoteRepository;
    @Mock
    private StudentServiceMapper studentServiceMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void shouldCreate() {
        // given
        var studentId = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phoneNumber = "+38599123456";
        var command = CreateStudentCommand.builder()
                .name(name)
                .email(email)
                .phone(phoneNumber)
                .build();
        var metrics = StudentProfile.Metrics.builder()
                .totalLessonsCompleted(0)
                .totalRevenue(BigDecimal.ZERO)
                .build();
        var studentProfile = StudentProfile.builder()
                .id(studentId)
                .name(name)
                .email(email)
                .phone(phoneNumber)
                .metrics(metrics)
                .build();
        when(studentRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
            var student = inv.getArgument(0);
            setId(student, studentId);
            return student;
        });
        when(studentServiceMapper.toProfile(any(Student.class), any(StudentProfile.Metrics.class))).thenReturn(studentProfile);

        // when
        studentService.create(command);

        // then
        verify(studentRepository).findByEmail(email);

        var captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        var s = captor.getValue();
        assertThat(s.getName()).isEqualTo(name);
        assertThat(s.getEmail()).isEqualTo(email);
        assertThat(s.getPhone()).isEqualTo(phoneNumber);
        verify(studentServiceMapper).toProfile(any(Student.class), any(StudentProfile.Metrics.class));
    }

    @Test
    void shouldNotCreateWhenEmailAlreadyExists() {
        // given
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phoneNumber = "+38599123456";
        var command = CreateStudentCommand.builder()
                .name(name)
                .email(email)
                .phone(phoneNumber)
                .build();
        var student = createStudent(name, email, phoneNumber);
        when(studentRepository.findByEmail(email)).thenReturn(Optional.of(student));

        // when
        assertThatThrownBy(() -> studentService.create(command)).isInstanceOf(EmailAlreadyInUseException.class);

        // then
        verify(studentRepository).findByEmail(email);
        verify(studentRepository, never()).save(any());
        verify(studentServiceMapper, never()).toProfile(any(), any());
    }

    @Test
    void shouldUpdateName() {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var newName = "Marko Markić - Markovinović";
        var email = "marko.markic@gmail.com";
        var phoneNumber = "+38599123456";
        var command = UpdateStudentCommand.builder()
                .studentId(id)
                .name(newName)
                .email(email)
                .phone(phoneNumber)
                .build();
        var student = Student.create(name, email, phoneNumber);
        setId(student, id);
        var metrics = StudentProfile.Metrics.builder()
                .totalLessonsCompleted(0)
                .totalRevenue(BigDecimal.ZERO)
                .build();
        var studentProfile = StudentProfile.builder()
                .id(id)
                .name(newName)
                .email(email)
                .phone(phoneNumber)
                .metrics(metrics)
                .build();
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail(email)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenAnswer(in -> {
            var s = (Student) in.getArgument(0);
            s.updateDetails(newName, email, phoneNumber);
            return s;
        });
        when(studentServiceMapper.toProfile(student, metrics)).thenReturn(studentProfile);

        // when
        studentService.update(command);

        // then
        verify(studentRepository).findById(id);
        verify(studentRepository).findByEmail(email);

        var captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo(newName);
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getPhone()).isEqualTo(phoneNumber);
        assertThat(saved.getId()).isEqualTo(id);

        verify(studentServiceMapper).toProfile(student, metrics);
    }

    @Test
    void shouldUpdateEmail() {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var newEmail = "marko.markic@protonmail.com";
        var phoneNumber = "+38599123456";
        var command = UpdateStudentCommand.builder()
                .studentId(id)
                .name(name)
                .email(newEmail)
                .phone(phoneNumber)
                .build();
        var student = Student.create(name, email, phoneNumber);
        setId(student, id);
        var metrics = StudentProfile.Metrics.builder()
                .totalLessonsCompleted(0)
                .totalRevenue(BigDecimal.ZERO)
                .build();
        var studentProfile = StudentProfile.builder()
                .id(id)
                .name(name)
                .email(email)
                .phone(phoneNumber)
                .metrics(metrics)
                .build();
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(studentRepository.save(student)).thenAnswer(in -> {
            var s = (Student) in.getArgument(0);
            s.updateDetails(name, newEmail, phoneNumber);
            return s;
        });
        when(studentServiceMapper.toProfile(student, metrics)).thenReturn(studentProfile);

        // when
        studentService.update(command);

        // then
        verify(studentRepository).findById(id);
        verify(studentRepository).findByEmail(newEmail);

        var captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo(name);
        assertThat(saved.getEmail()).isEqualTo(newEmail);
        assertThat(saved.getPhone()).isEqualTo(phoneNumber);
        assertThat(saved.getId()).isEqualTo(id);

        assertThat(student.getName()).isEqualTo(name);
        assertThat(student.getEmail()).isEqualTo(newEmail);
        assertThat(student.getPhone()).isEqualTo(phoneNumber);

        verify(studentServiceMapper).toProfile(student, metrics);
    }

    @Test
    void shouldUpdatePhone() {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phoneNumber = "+38599123456";
        var newPhoneNumber = "+38599654321";
        var command = UpdateStudentCommand.builder()
                .studentId(id)
                .name(name)
                .email(email)
                .phone(newPhoneNumber)
                .build();
        var student = Student.create(name, email, phoneNumber);
        setId(student, id);
        var metrics = StudentProfile.Metrics.builder()
                .totalLessonsCompleted(0)
                .totalRevenue(BigDecimal.ZERO)
                .build();
        var studentProfile = StudentProfile.builder()
                .id(id)
                .name(name)
                .email(email)
                .phone(phoneNumber)
                .metrics(metrics)
                .build();
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail(email)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenAnswer(in -> {
            var s = (Student) in.getArgument(0);
            s.updateDetails(name, email, newPhoneNumber);
            return s;
        });
        when(studentServiceMapper.toProfile(student, metrics)).thenReturn(studentProfile);

        // when
        studentService.update(command);

        // then
        verify(studentRepository).findById(id);
        verify(studentRepository).findByEmail(email);

        var captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo(name);
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getPhone()).isEqualTo(newPhoneNumber);
        assertThat(saved.getId()).isEqualTo(id);

        assertThat(student.getName()).isEqualTo(name);
        assertThat(student.getEmail()).isEqualTo(email);
        assertThat(student.getPhone()).isEqualTo(newPhoneNumber);

        verify(studentServiceMapper).toProfile(student, metrics);
    }

    @Test
    void shouldNotUpdateWhenStudentNotExists() {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var newName = "Marko Markić - Markovinović";
        var email = "marko.markic@gmail.com";
        var phoneNumber = "+38599123456";
        var command = UpdateStudentCommand.builder()
                .studentId(id)
                .name(newName)
                .email(email)
                .phone(phoneNumber)
                .build();
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> studentService.update(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(studentRepository).findById(id);
        verify(studentRepository, never()).findByEmail(email);
        verify(studentRepository, never()).save(any());
        verify(studentServiceMapper, never()).toProfile(any(), any());
    }

    @Test
    void shouldNotUpdateWhenNewEmailNotUnique() {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var newEmail = "marko.markic@protonmail.com";
        var phoneNumber = "+38599123456";
        var command = UpdateStudentCommand.builder()
                .studentId(id)
                .name(name)
                .email(newEmail)
                .phone(phoneNumber)
                .build();
        var student = Student.create(name, email, phoneNumber);
        setId(student, id);
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail(newEmail)).thenReturn(Optional.of(createStudent("Sinko Sinić", newEmail, "+38598555555")));

        // when
        assertThatThrownBy(() -> studentService.update(command)).isInstanceOf(EmailAlreadyInUseException.class);

        // then
        verify(studentRepository).findById(id);
        verify(studentRepository).findByEmail(newEmail);
        verify(studentRepository, never()).save(any());
        verify(studentServiceMapper, never()).toProfile(any(), any());
    }

    @Test
    void shouldSearchAll() {
        // given
        var query = "";
        var name1 = "Marko Markić";
        var email1 = "marko.markic@gmail.com";
        var phone1 = "+38599123456";
        var name2 = "Sinko Sinkić";
        var email2 = "sinko.sinkic@protonmail.com";
        var phone2 = "+38599654321";
        var student1 = createStudent(name1, email1, phone1);
        var student2 = createStudent(name2, email2, phone2);
        var searchRes1 = StudentSearchResult.builder()
                .id(student1.getId())
                .name(name1)
                .email(email1)
                .phone(phone1)
                .build();
        var searchRes2 = StudentSearchResult.builder()
                .id(student2.getId())
                .name(name2)
                .email(email2)
                .phone(phone2)
                .build();
        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));
        when(studentServiceMapper.toSearchResult(student1)).thenReturn(searchRes1);
        when(studentServiceMapper.toSearchResult(student2)).thenReturn(searchRes2);

        // when
        var result = studentService.search(query);

        // then
        verify(studentRepository).findAll();
        verify(studentRepository, never()).searchByNameOrEmail(anyString());
        verify(studentServiceMapper).toSearchResult(student1);
        verify(studentServiceMapper).toSearchResult(student2);
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldSearchByString() {
        // given
        var query = "Marko";
        var name1 = "Marko Markić";
        var email1 = "marko.markic@gmail.com";
        var phone1 = "+38599123456";
        var student1 = createStudent(name1, email1, phone1);
        var searchRes1 = StudentSearchResult.builder()
                .id(student1.getId())
                .name(name1)
                .email(email1)
                .phone(phone1)
                .build();
        when(studentRepository.searchByNameOrEmail(query.toLowerCase())).thenReturn(List.of(student1));
        when(studentServiceMapper.toSearchResult(student1)).thenReturn(searchRes1);

        // when
        var result = studentService.search(query);

        // then
        verify(studentRepository, never()).findAll();
        verify(studentRepository).searchByNameOrEmail(query.toLowerCase());
        verify(studentServiceMapper).toSearchResult(student1);
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnStudentProfile() {
        // given
        var studentId = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phoneNumber = "+38599123456";
        var student = createStudent(name, email, phoneNumber);
        setId(student, studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(appointment.getState()).thenReturn(AppointmentState.COMPLETED);
        var appointments = List.of(appointment);
        var paymentRecord = mock(PaymentRecord.class);
        when(paymentRecord.getAmount()).thenReturn(BigDecimal.TEN);
        var timeSlot = mock(TimeSlot.class);
        var slotDate = LocalDate.of(2026, 6, 22);
        var metrics = StudentProfile.Metrics.builder()
                .totalLessonsCompleted(1)
                .totalRevenue(BigDecimal.TEN)
                .lastLessonDate(slotDate)
                .build();
        var studentProfile = StudentProfile.builder()
                .id(studentId)
                .name(name)
                .email(email)
                .phone(phoneNumber)
                .metrics(metrics)
                .build();
        when(timeSlot.getSlotDate()).thenReturn(slotDate);
        when(appointment.getTimeSlot()).thenReturn(timeSlot);
        when(appointmentRepository.findByStudentIdOrderByCreatedAtDesc(studentId)).thenReturn(appointments);
        when(paymentRecordRepository.findByAppointmentId(appointmentId)).thenReturn(Optional.of(paymentRecord));
        when(studentServiceMapper.toProfile(student, metrics)).thenReturn(studentProfile);

        // when
        studentService.getProfile(studentId);

        // then
        verify(studentRepository).findById(studentId);
        verify(appointmentRepository).findByStudentIdOrderByCreatedAtDesc(studentId);
        verify(paymentRecordRepository).findByAppointmentId(appointmentId);
        verify(studentServiceMapper).toProfile(student, metrics);
    }

    @Test
    void shouldReturnStudentProfileWithEmptyMetrics() {
        // given
        var studentId = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phoneNumber = "+38599123456";
        var student = createStudent(name, email, phoneNumber);
        setId(student, studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        var appointment = mock(Appointment.class);
        when(appointment.getState()).thenReturn(AppointmentState.RESERVED);
        var appointments = List.of(appointment);
        var metrics = StudentProfile.Metrics.builder()
                .totalLessonsCompleted(0)
                .totalRevenue(BigDecimal.ZERO)
                .build();
        var studentProfile = StudentProfile.builder()
                .id(studentId)
                .name(name)
                .email(email)
                .phone(phoneNumber)
                .metrics(metrics)
                .build();
        when(appointmentRepository.findByStudentIdOrderByCreatedAtDesc(studentId)).thenReturn(appointments);
        when(studentServiceMapper.toProfile(student, metrics)).thenReturn(studentProfile);

        // when
        studentService.getProfile(studentId);

        // then
        verify(studentRepository).findById(studentId);
        verify(appointmentRepository).findByStudentIdOrderByCreatedAtDesc(studentId);
        verify(paymentRecordRepository, never()).findByAppointmentId(any());
        verify(studentServiceMapper).toProfile(student, metrics);
    }

    @Test
    void shouldNotReturnProfileWhenStudentNotExists() {
        // given
        var studentId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> studentService.getProfile(studentId)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(studentRepository).findById(studentId);
        verify(appointmentRepository, never()).findByStudentIdOrderByCreatedAtDesc(any());
        verify(paymentRecordRepository, never()).findByAppointmentId(any());
        verify(studentServiceMapper, never()).toProfile(any(), any());
    }

    @Test
    void shouldGetAppointmentHistory() {
        // given
        var studentId = UUID.randomUUID();
        var student = mock(Student.class);
        var appointmentId = UUID.randomUUID();
        var timeSlotDate = LocalDate.of(2026, 6, 22);
        var timeSlotStartTime = LocalTime.of(12, 0);
        var serviceCategoryName = "Matematika za osnovnu školu";
        var appointmentState = AppointmentState.COMPLETED;
        var appointment = mock(Appointment.class);
        var appointments = List.of(appointment);
        var appointmentHistory = AppointmentHistory.builder()
                .appointmentId(appointmentId)
                .date(timeSlotDate)
                .startTime(timeSlotStartTime)
                .serviceCategoryName(serviceCategoryName)
                .state(appointmentState)
                .build();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(appointmentRepository.findByStudentIdOrderByCreatedAtDesc(studentId)).thenReturn(appointments);
        when(studentServiceMapper.toAppointmentHistory(appointment)).thenReturn(appointmentHistory);

        // when
        studentService.getAppointmentHistory(studentId);

        // then
        verify(studentRepository).findById(studentId);
        verify(appointmentRepository).findByStudentIdOrderByCreatedAtDesc(studentId);
        verify(studentServiceMapper).toAppointmentHistory(appointment);
    }

    @Test
    void shouldNotGetAppointmentHistoryWhenStudentNotExists() {
        // given
        var studentId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> studentService.getAppointmentHistory(studentId)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(studentRepository).findById(studentId);
        verify(appointmentRepository, never()).findByStudentIdOrderByCreatedAtDesc(any());
        verify(studentServiceMapper, never()).toAppointmentHistory(any());
    }

    @Test
    void shouldAddNote() {
        // given
        var studentId = UUID.randomUUID();
        var note = "Koncentracija pada pri kraju sata";
        var command = AddNoteCommand.builder()
                .studentId(studentId)
                .content(note)
                .build();
        var student = mock(Student.class);
        var noteEntry = mock(NoteEntry.class);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentNoteRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(studentServiceMapper.toNoteEntry(any(StudentNote.class))).thenReturn(noteEntry);

        // when
        var result = studentService.addNote(command);

        // then
        verify(studentRepository).findById(studentId);
        var capture = ArgumentCaptor.forClass(StudentNote.class);
        verify(studentNoteRepository).save(capture.capture());
        var savedNote = capture.getValue();
        assertThat(savedNote.getStudent()).isEqualTo(student);
        assertThat(savedNote.getContent()).isEqualTo(note);

        verify(studentServiceMapper).toNoteEntry(any(StudentNote.class));
    }

    @Test
    void shouldUpdateNote() {
        // given
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var noteContent = "Napomena koja se mijenja";
        var command = UpdateNoteCommand.builder()
                .studentId(studentId)
                .noteId(noteId)
                .content(noteContent)
                .build();
        var studentNote = mock(StudentNote.class);
        var student = mock(Student.class);
        var noteEntry = mock(NoteEntry.class);
        when(studentNote.getStudent()).thenReturn(student);
        when(student.getId()).thenReturn(studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentNoteRepository.findById(noteId)).thenReturn(Optional.of(studentNote));
        when(studentNoteRepository.save(studentNote)).thenAnswer(i -> i.getArgument(0));
        when(studentServiceMapper.toNoteEntry(studentNote)).thenReturn(noteEntry);

        // when
        studentService.updateNote(command);

        // then
        verify(studentRepository).findById(studentId);
        verify(studentNoteRepository).findById(noteId);
        verify(studentNote).updateContent(noteContent);
        verify(studentNoteRepository).save(studentNote);
        verify(studentServiceMapper).toNoteEntry(studentNote);
    }

    @Test
    void shouldNotUpdateNotWhenStudentNotExists() {
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var noteContent = "Napomena koja se mijenja";
        var command = UpdateNoteCommand.builder()
                .studentId(studentId)
                .noteId(noteId)
                .content(noteContent)
                .build();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> studentService.updateNote(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(studentRepository).findById(studentId);
        verify(studentNoteRepository, never()).findById(any());
        verify(studentNoteRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateNoteWhenNoteNotExists() {
        // given
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var noteContent = "Napomena koja se mijenja";
        var command = UpdateNoteCommand.builder()
                .studentId(studentId)
                .noteId(noteId)
                .content(noteContent)
                .build();
        var student = mock(Student.class);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentNoteRepository.findById(noteId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> studentService.updateNote(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(studentRepository).findById(studentId);
        verify(studentNoteRepository).findById(noteId);
        verify(studentNoteRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateNoteWhenInvalidOwnership() {
        // given
        var differentStudentId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var noteContent = "Napomena koja se mijenja";
        var command = UpdateNoteCommand.builder()
                .studentId(studentId)
                .noteId(noteId)
                .content(noteContent)
                .build();
        var studentNote = mock(StudentNote.class);
        var student = mock(Student.class);
        var differentStudent = mock(Student.class);
        when(studentNote.getStudent()).thenReturn(differentStudent);
        when(differentStudent.getId()).thenReturn(differentStudentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentNoteRepository.findById(noteId)).thenReturn(Optional.of(studentNote));

        // when
        assertThatThrownBy(() -> studentService.updateNote(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(studentRepository).findById(studentId);
        verify(studentNoteRepository).findById(noteId);
        verify(studentNote, never()).updateContent(any());
        verify(studentNoteRepository, never()).save(studentNote);
    }

    @Test
    void shouldGetStudentNotes() {
        // given
        var studentId = UUID.randomUUID();
        var student = mock(Student.class);
        var note = mock(StudentNote.class);
        var notes = List.of(note);
        var noteEntry = mock(NoteEntry.class);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentNoteRepository.findByStudentIdOrderByCreatedAtDesc(studentId)).thenReturn(notes);
        when(studentServiceMapper.toNoteEntry(note)).thenReturn(noteEntry);

        // when
        studentService.getNotes(studentId);

        // then
        verify(studentRepository).findById(studentId);
        verify(studentNoteRepository).findByStudentIdOrderByCreatedAtDesc(studentId);
        verify(studentServiceMapper).toNoteEntry(note);
    }

    @Test
    void shouldNotGetStudentNotesWhenStudentNotExists() {
        // given
        var studentId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> studentService.getNotes(studentId)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(studentRepository).findById(studentId);
        verify(studentNoteRepository, never()).findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Test
    void shouldNotAddNoteWhenStudentNotExists() {
        // given
        var studentId = UUID.randomUUID();
        var note = "Koncentracija pada pri kraju sata";
        var command = AddNoteCommand.builder()
                .studentId(studentId)
                .content(note)
                .build();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> studentService.addNote(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(studentRepository).findById(studentId);
        verify(studentNoteRepository, never()).save(any());
    }

    // Helper methods

    private Student createStudent(String name, String email, String phone) {
        var student = Student.create(name, email, phone);
        setId(student, UUID.randomUUID());
        return student;
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}