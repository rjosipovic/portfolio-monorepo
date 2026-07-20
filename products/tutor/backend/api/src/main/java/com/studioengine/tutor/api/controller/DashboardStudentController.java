package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.student.AppointmentHistoryResponse;
import com.studioengine.tutor.api.dto.student.AssignBenefitRequest;
import com.studioengine.tutor.api.dto.student.BenefitResponse;
import com.studioengine.tutor.api.dto.student.CreateStudentRequest;
import com.studioengine.tutor.api.dto.student.NoteRequest;
import com.studioengine.tutor.api.dto.student.NoteResponse;
import com.studioengine.tutor.api.dto.student.StudentProfileResponse;
import com.studioengine.tutor.api.dto.student.UpdateStudentRequest;
import com.studioengine.tutor.api.dto.summary.StudentSummary;
import com.studioengine.tutor.api.mapper.StudentMapper;
import com.studioengine.tutor.benefit.BenefitService;
import com.studioengine.tutor.student.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard/students")
@RequiredArgsConstructor
@Slf4j
public class DashboardStudentController {

    private final StudentService studentService;
    private final BenefitService benefitService;
    private final StudentMapper studentMapper;

    @GetMapping
    public ResponseEntity<List<StudentSummary>> search(@RequestParam(required = false, name = "query") String query) {
        log.info("GET /dashboard/students query={}", query);
        var students = studentService.search(query).stream()
                .map(studentMapper::toStudentSummary)
                .toList();
        return ResponseEntity.ok(students);
    }

    @PostMapping
    public ResponseEntity<StudentProfileResponse> create(@Valid @RequestBody CreateStudentRequest request) {
        log.info("POST /dashboard/students email={}", request.getEmail());

        var command = studentMapper.toCreateStudentCommand(request);
        var profile = studentService.create(command);
        var studentProfile = studentMapper.toProfileResponse(profile);
        var location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(profile.getId())
                .toUri();
        return ResponseEntity.created(location).body(studentProfile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentProfileResponse> getProfile(@PathVariable UUID id) {
        log.info("GET /dashboard/students/{}", id);

        var profile = studentService.getProfile(id);
        var studentProfile = studentMapper.toProfileResponse(profile);
        return ResponseEntity.ok(studentProfile);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentProfileResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateStudentRequest request) {
        log.info("PUT /dashboard/students/{}", id);

        var command = studentMapper.toUpdateStudentCommand(request, id);
        var profile = studentService.update(command);
        var studentProfile = studentMapper.toProfileResponse(profile);
        return ResponseEntity.ok(studentProfile);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentHistoryResponse>> getAppointmentHistory(@PathVariable UUID id) {
        log.info("GET /dashboard/students/{}/appointments", id);

        var response = studentService.getAppointmentHistory(id).stream()
                .map(studentMapper::toAppointmentHistoryResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<NoteResponse> addNote(
            @PathVariable(name = "id") UUID studentId,
            @Valid @RequestBody NoteRequest request
    ) {
        log.info("POST /dashboard/students/{}/notes", studentId);

        var command = studentMapper.toAddNoteCommand(request, studentId);
        var note = studentService.addNote(command);
        var noteResponse = studentMapper.toNoteResponse(note);

        return ResponseEntity.status(HttpStatus.CREATED).body(noteResponse);
    }

    @PutMapping("/{id}/notes/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable(name = "id") UUID studentId,
            @PathVariable(name = "noteId") UUID noteId,
            @Valid @RequestBody NoteRequest request
    ) {
        log.info("PUT /dashboard/students/{}/notes/{}", studentId, noteId);

        var command = studentMapper.toUpdateNoteCommand(request, studentId, noteId);
        var note = studentService.updateNote(command);
        var noteResponse = studentMapper.toNoteResponse(note);
        return ResponseEntity.ok(noteResponse);
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<NoteResponse>> getNotes(@PathVariable(name = "id") UUID studentId) {
        log.info("GET /dashboard/students/{}/notes", studentId);

        var responseNotes = studentService.getNotes(studentId).stream()
                .map(studentMapper::toNoteResponse)
                .toList();
        return ResponseEntity.ok(responseNotes);
    }

    @GetMapping("/{id}/benefits")
    public ResponseEntity<List<BenefitResponse>> getBenefits(@PathVariable(name = "id") UUID studentId) {
        log.info("GET /dashboard/students/{}/benefits", studentId);

        var benefits = benefitService.getBenefits(studentId);
        var response = studentMapper.toBenefitResponseList(benefits);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/benefits")
    public ResponseEntity<BenefitResponse> assignBenefit(
            @PathVariable(name = "id") UUID studentId,
            @Valid @RequestBody AssignBenefitRequest request) {

        var command = studentMapper.toAssignBenefitCommand(request, studentId);
        var assignedBenefit = benefitService.assign(command);
        var response = studentMapper.toBenefitResponse(assignedBenefit);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
