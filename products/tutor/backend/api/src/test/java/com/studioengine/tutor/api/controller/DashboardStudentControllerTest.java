package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.student.AppointmentHistoryResponse;
import com.studioengine.tutor.api.dto.student.CreateStudentRequest;
import com.studioengine.tutor.api.dto.student.NoteRequest;
import com.studioengine.tutor.api.dto.student.NoteResponse;
import com.studioengine.tutor.api.dto.student.StudentProfileResponse;
import com.studioengine.tutor.api.dto.student.UpdateStudentRequest;
import com.studioengine.tutor.api.dto.summary.StudentSummary;
import com.studioengine.tutor.api.mapper.StudentMapper;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.EmailAlreadyInUseException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.student.AddNoteCommand;
import com.studioengine.tutor.student.AppointmentHistory;
import com.studioengine.tutor.student.CreateStudentCommand;
import com.studioengine.tutor.student.NoteEntry;
import com.studioengine.tutor.student.StudentProfile;
import com.studioengine.tutor.student.StudentSearchResult;
import com.studioengine.tutor.student.StudentService;
import com.studioengine.tutor.student.UpdateNoteCommand;
import com.studioengine.tutor.student.UpdateStudentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardStudentControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private DashboardStudentController dashboardStudentController;

    private MockMvc mockMvc;

    private JacksonTester<List<StudentSummary>> studentSummaryJson;
    private JacksonTester<CreateStudentRequest> createStudentRequestJson;
    private JacksonTester<StudentProfileResponse> studentProfileResponseJson;
    private JacksonTester<ErrorResponse> errorResponseJson;
    private JacksonTester<UpdateStudentRequest> updateStudentRequestJson;
    private JacksonTester<List<AppointmentHistoryResponse>> appointmentHistoryResponseListJson;
    private JacksonTester<NoteRequest> noteRequestJson;
    private JacksonTester<NoteResponse> noteResponseJson;
    private JacksonTester<List<NoteResponse>> noteResponseListJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardStudentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // GET dashboard/students
    @Test
    void shouldReturnSearchResults() throws Exception {
        // given
        var query = "ivan";
        var studentId = UUID.randomUUID();
        var studentName = "Marko Markić";
        var studentEmail = "marko.markic@gmail.com";
        var studentPhone = "+38599123456";
        var student = mock(StudentSearchResult.class);
        var students = List.of(student);
        when(studentService.search(query)).thenReturn(students);

        var studentSummary = StudentSummary.builder()
                        .id(studentId)
                        .name(studentName)
                        .email(studentEmail)
                        .phone(studentPhone)
                .build();
        when(studentMapper.toStudentSummary(student)).thenReturn(studentSummary);

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/students")
                .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("query", query))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(studentService).search(query);
        verify(studentMapper).toStudentSummary(student);
        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(studentSummaryJson.write(List.of(studentSummary)).getJson());
    }

    @Test
    void shouldReturnEmptySearchResults() throws Exception {
        // given
        var query = "ivan";
        when(studentService.search(query)).thenReturn(List.of());

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("query", query))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(studentService).search(query);
        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(studentSummaryJson.write(List.of()).getJson());
    }

    // POST dashboard/students

    @Test
    void shouldCreateStudent() throws Exception {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phone = "+38599123456";
        var createStudentRequest = CreateStudentRequest.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        var createStudentCommand = CreateStudentCommand.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentMapper.toCreateStudentCommand(createStudentRequest)).thenReturn(createStudentCommand);

        var studentProfile = StudentProfile.builder()
                .id(id)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentService.create(createStudentCommand)).thenReturn(studentProfile);

        var studentProfileResponse = StudentProfileResponse.builder()
                .id(id)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentMapper.toProfileResponse(studentProfile)).thenReturn(studentProfileResponse);

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createStudentRequestJson.write(createStudentRequest).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toCreateStudentCommand(createStudentRequest);
        verify(studentService).create(createStudentCommand);
        verify(studentMapper).toProfileResponse(studentProfile);

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(studentProfileResponseJson.write(studentProfileResponse).getJson());
        assertThat(response.getHeader("Location")).endsWith("/api/v1/dashboard/students/%s".formatted(id));
    }

    @Test
    void shouldNotCreateStudentWhenEmailNotUnique() throws Exception {
        // given
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phone = "+38599123456";
        var createStudentRequest = CreateStudentRequest.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        var createStudentCommand = CreateStudentCommand.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentMapper.toCreateStudentCommand(createStudentRequest)).thenReturn(createStudentCommand);

        var reason = "Email already in use: %s".formatted(email);
        when(studentService.create(createStudentCommand)).thenThrow(new EmailAlreadyInUseException(reason));

        var errorResponse = ErrorResponse.builder()
                .message(ErrorCode.EMAIL_ALREADY_IN_USE.getMessage())
                .code(ErrorCode.EMAIL_ALREADY_IN_USE.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createStudentRequestJson.write(createStudentRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toCreateStudentCommand(createStudentRequest);
        verify(studentService).create(createStudentCommand);
        verify(studentMapper, never()).toProfileResponse(any());

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
    }

    @Test
    void shouldUpdateStudent() throws Exception {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phone = "+38599123456";
        var updateStudentRequest = UpdateStudentRequest.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        var updateStudentCommand = UpdateStudentCommand.builder()
                .studentId(id)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentMapper.toUpdateStudentCommand(updateStudentRequest, id)).thenReturn(updateStudentCommand);

        var studentProfile = StudentProfile.builder()
                .id(id)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentService.update(updateStudentCommand)).thenReturn(studentProfile);

        var studentProfileResponse = StudentProfileResponse.builder()
                .id(id)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentMapper.toProfileResponse(studentProfile)).thenReturn(studentProfileResponse);

        // when
        var response = mockMvc.perform(put("/api/v1/dashboard/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateStudentRequestJson.write(updateStudentRequest).getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toUpdateStudentCommand(updateStudentRequest, id);
        verify(studentService).update(updateStudentCommand);
        verify(studentMapper).toProfileResponse(studentProfile);

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(studentProfileResponseJson.write(studentProfileResponse).getJson());
    }

    @Test
    void shouldNotUpdateStudentWhenStudentNotExists() throws Exception {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phone = "+38599123456";
        var updateStudentRequest = UpdateStudentRequest.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        var updateStudentCommand = UpdateStudentCommand.builder()
                .studentId(id)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentMapper.toUpdateStudentCommand(updateStudentRequest, id)).thenReturn(updateStudentCommand);

        var reason = "Student not found: %s".formatted(id);
        when(studentService.update(updateStudentCommand)).thenThrow(new ResourceNotFoundException(reason));

        var errorResponse = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(put("/api/v1/dashboard/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateStudentRequestJson.write(updateStudentRequest).getJson()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toUpdateStudentCommand(updateStudentRequest, id);
        verify(studentService).update(updateStudentCommand);
        verify(studentMapper, never()).toProfileResponse(any());

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
    }

    @Test
    void shouldNotUpdateStudentWhenEmailNotUnique() throws Exception {
        // given
        var id = UUID.randomUUID();
        var name = "Marko Markić";
        var email = "marko.markic@gmail.com";
        var phone = "+38599123456";
        var updateStudentRequest = UpdateStudentRequest.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        var updateStudentCommand = UpdateStudentCommand.builder()
                .studentId(id)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        when(studentMapper.toUpdateStudentCommand(updateStudentRequest, id)).thenReturn(updateStudentCommand);

        var reason = "Email already in use: %s".formatted(email);
        when(studentService.update(updateStudentCommand)).thenThrow(new EmailAlreadyInUseException(reason));

        var errorResponse = ErrorResponse.builder()
                .message(ErrorCode.EMAIL_ALREADY_IN_USE.getMessage())
                .code(ErrorCode.EMAIL_ALREADY_IN_USE.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(put("/api/v1/dashboard/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateStudentRequestJson.write(updateStudentRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toUpdateStudentCommand(updateStudentRequest, id);
        verify(studentService).update(updateStudentCommand);
        verify(studentMapper, never()).toProfileResponse(any());

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
    }

    @Test
    void shouldGetAppointmentHistory() throws Exception {
        // given
        var id = UUID.randomUUID();

        var appointmentId = UUID.randomUUID();
        var date = LocalDate.of(2026, 6, 22);
        var startTime = LocalTime.of(12, 0);
        var serviceCategory = "Pripreme za maturu";
        var appointmentState = AppointmentState.COMPLETED;
        var appointment = AppointmentHistory.builder()
                .appointmentId(appointmentId)
                .date(date)
                .startTime(startTime)
                .serviceCategoryName(serviceCategory)
                .state(appointmentState)
                .build();
        var appointments = List.of(appointment);
        when(studentService.getAppointmentHistory(id)).thenReturn(appointments);

        var appointmentHistoryResponse = AppointmentHistoryResponse.builder()
                .appointmentId(appointmentId)
                .date(date)
                .startTime(startTime)
                .serviceCategoryName(serviceCategory)
                .state(appointmentState.name())
                .build();
        when(studentMapper.toAppointmentHistoryResponse(appointment)).thenReturn(appointmentHistoryResponse);

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/students/{id}/appointments", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(studentService).getAppointmentHistory(id);
        verify(studentMapper).toAppointmentHistoryResponse(appointment);
        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(appointmentHistoryResponseListJson.write(List.of(appointmentHistoryResponse)).getJson());
    }

    @Test
    void shouldGetEmptyAppointmentHistory() throws Exception {
        // given
        var id = UUID.randomUUID();

        when(studentService.getAppointmentHistory(id)).thenReturn(List.of());

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/students/{id}/appointments", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(studentService).getAppointmentHistory(id);
        verify(studentMapper, never()).toAppointmentHistoryResponse(any());
        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(appointmentHistoryResponseListJson.write(List.of()).getJson());
    }

    @Test
    void shouldNotGetAppointmentHistoryWhenStudentNotExists() throws Exception {
        // given
        var id = UUID.randomUUID();

        var reason = "Student not found: %s".formatted(id);
        when(studentService.getAppointmentHistory(id)).thenThrow(new ResourceNotFoundException(reason));

        var errorResponse = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/students/{id}/appointments", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(studentService).getAppointmentHistory(id);
        verify(studentMapper, never()).toAppointmentHistoryResponse(any());
        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
    }

    @Test
    void shouldAddNote() throws Exception {
        // given
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var note = "Napomena o studentu";
        var noteRequest = NoteRequest.builder()
                .content(note)
                .build();
        var noteCommand = AddNoteCommand.builder()
                .studentId(studentId)
                .content(note)
                .build();
        when(studentMapper.toAddNoteCommand(noteRequest, studentId)).thenReturn(noteCommand);

        var studentNote = NoteEntry.builder()
                .id(noteId)
                .content(note)
                .build();
        when(studentService.addNote(noteCommand)).thenReturn(studentNote);

        var noteResponse = NoteResponse.builder()
                .content(note)
                .build();
        when(studentMapper.toNoteResponse(studentNote)).thenReturn(noteResponse);

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/students/{id}/notes", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteRequestJson.write(noteRequest).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toAddNoteCommand(noteRequest, studentId);
        verify(studentService).addNote(noteCommand);
        verify(studentMapper).toNoteResponse(studentNote);

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(noteResponseJson.write(noteResponse).getJson());
    }

    @Test
    void shouldNotAddNoteWhenStudentNotExists() throws Exception {
        // given
        var studentId = UUID.randomUUID();
        var note = "Napomena o studentu";
        var noteRequest = NoteRequest.builder()
                .content(note)
                .build();
        var noteCommand = AddNoteCommand.builder()
                .studentId(studentId)
                .content(note)
                .build();
        when(studentMapper.toAddNoteCommand(noteRequest, studentId)).thenReturn(noteCommand);

        var reason = "Student not found: %s".formatted(studentId);
        when(studentService.addNote(noteCommand)).thenThrow(new ResourceNotFoundException(reason));

        var errorResponse = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/students/{id}/notes", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteRequestJson.write(noteRequest).getJson()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toAddNoteCommand(noteRequest, studentId);
        verify(studentService).addNote(noteCommand);
        verify(studentMapper, never()).toNoteResponse(any());

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
    }

    @Test
    void shouldUpdateNote() throws Exception {
        // given
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var note = "Nova napomena o studentu";
        var noteRequest = NoteRequest.builder()
                .content(note)
                .build();
        var noteCommand = UpdateNoteCommand.builder()
                .studentId(studentId)
                .noteId(noteId)
                .content(note)
                .build();
        when(studentMapper.toUpdateNoteCommand(noteRequest, studentId, noteId)).thenReturn(noteCommand);

        var studentNote = NoteEntry.builder()
                .id(noteId)
                .content(note)
                .build();
        when(studentService.updateNote(noteCommand)).thenReturn(studentNote);

        var noteResponse = NoteResponse.builder()
                .content(note)
                .build();
        when(studentMapper.toNoteResponse(studentNote)).thenReturn(noteResponse);

        // when
        var response = mockMvc.perform(put("/api/v1/dashboard/students/{id}/notes/{noteId}", studentId, noteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteRequestJson.write(noteRequest).getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toUpdateNoteCommand(noteRequest, studentId, noteId);
        verify(studentService).updateNote(noteCommand);
        verify(studentMapper).toNoteResponse(studentNote);

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(noteResponseJson.write(noteResponse).getJson());
    }

    @Test
    void shouldNotUpdateNoteWhenStudentNotExists() throws Exception {
        // given
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var note = "Nova napomena o studentu";
        var noteRequest = NoteRequest.builder()
                .content(note)
                .build();
        var noteCommand = UpdateNoteCommand.builder()
                .studentId(studentId)
                .noteId(noteId)
                .content(note)
                .build();
        when(studentMapper.toUpdateNoteCommand(noteRequest, studentId, noteId)).thenReturn(noteCommand);

        var reason = "Student not found: %s".formatted(studentId);
        when(studentService.updateNote(noteCommand)).thenThrow(new ResourceNotFoundException(reason));

        var errorResponse = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(put("/api/v1/dashboard/students/{id}/notes/{noteId}", studentId, noteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteRequestJson.write(noteRequest).getJson()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toUpdateNoteCommand(noteRequest, studentId, noteId);
        verify(studentService).updateNote(noteCommand);
        verify(studentMapper, never()).toNoteResponse(any());

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
    }

    @Test
    void shouldNotUpdateNoteWhenNoteNotExistsOrInvalidOwnership() throws Exception {
        // given
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var note = "Nova napomena o studentu";
        var noteRequest = NoteRequest.builder()
                .content(note)
                .build();
        var noteCommand = UpdateNoteCommand.builder()
                .studentId(studentId)
                .noteId(noteId)
                .content(note)
                .build();
        when(studentMapper.toUpdateNoteCommand(noteRequest, studentId, noteId)).thenReturn(noteCommand);

        var reason = "Note not found: %s".formatted(noteId);
        when(studentService.updateNote(noteCommand)).thenThrow(new ResourceNotFoundException(reason));

        var errorResponse = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(put("/api/v1/dashboard/students/{id}/notes/{noteId}", studentId, noteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteRequestJson.write(noteRequest).getJson()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(studentMapper).toUpdateNoteCommand(noteRequest, studentId, noteId);
        verify(studentService).updateNote(noteCommand);
        verify(studentMapper, never()).toNoteResponse(any());

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
    }

    @Test
    void shouldGetNotes() throws Exception {
        // given
        var studentId = UUID.randomUUID();
        var noteId = UUID.randomUUID();
        var content = "Napomena";
        var createdAt = OffsetDateTime.now().minusDays(1);
        var updatedAt = OffsetDateTime.now().minusDays(1);
        var noteEntry = NoteEntry.builder()
                .id(noteId)
                .content(content)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        var notes = List.of(noteEntry);
        when(studentService.getNotes(studentId)).thenReturn(notes);

        var noteResponse = NoteResponse.builder()
                .id(noteId)
                .content(content)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        when(studentMapper.toNoteResponse(noteEntry)).thenReturn(noteResponse);

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/students/{id}/notes", studentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(studentService).getNotes(studentId);
        verify(studentMapper).toNoteResponse(noteEntry);

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(noteResponseListJson.write(List.of(noteResponse)).getJson());
    }

    @Test
    void shouldNotGetNotesWhenStudentNotExists() throws Exception {
        // given
        var studentId = UUID.randomUUID();

        var reason = "Student not found: %s".formatted(studentId);
        when(studentService.getNotes(studentId)).thenThrow(new ResourceNotFoundException(reason));

        var errorResponse = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(get("/api/v1/dashboard/students/{id}/notes", studentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(studentService).getNotes(studentId);
        verify(studentMapper, never()).toNoteResponse(any());

        assertThat(response).isNotNull();
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
    }
}