package com.studioengine.tutor.api.mapper;

import com.studioengine.tutor.api.dto.student.AppointmentHistoryResponse;
import com.studioengine.tutor.api.dto.student.AssignBenefitRequest;
import com.studioengine.tutor.api.dto.student.BenefitResponse;
import com.studioengine.tutor.api.dto.student.CreateStudentRequest;
import com.studioengine.tutor.api.dto.student.NoteRequest;
import com.studioengine.tutor.api.dto.student.NoteResponse;
import com.studioengine.tutor.api.dto.student.StudentProfileResponse;
import com.studioengine.tutor.api.dto.student.UpdateStudentRequest;
import com.studioengine.tutor.api.dto.summary.StudentSummary;
import com.studioengine.tutor.benefit.AssignBenefitCommand;
import com.studioengine.tutor.benefit.AssignedBenefit;
import com.studioengine.tutor.benefit.BenefitEntry;
import com.studioengine.tutor.student.AddNoteCommand;
import com.studioengine.tutor.student.AppointmentHistory;
import com.studioengine.tutor.student.CreateStudentCommand;
import com.studioengine.tutor.student.NoteEntry;
import com.studioengine.tutor.student.StudentProfile;
import com.studioengine.tutor.student.StudentSearchResult;
import com.studioengine.tutor.student.UpdateNoteCommand;
import com.studioengine.tutor.student.UpdateStudentCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentSummary toStudentSummary(StudentSearchResult searchResult);

    CreateStudentCommand toCreateStudentCommand(CreateStudentRequest request);
    StudentProfileResponse toProfileResponse(StudentProfile profile);
    UpdateStudentCommand toUpdateStudentCommand(UpdateStudentRequest request, UUID studentId);

    AppointmentHistoryResponse toAppointmentHistoryResponse(AppointmentHistory history);

    AddNoteCommand toAddNoteCommand(NoteRequest request, UUID studentId);
    NoteResponse toNoteResponse(NoteEntry noteEntry);
    UpdateNoteCommand toUpdateNoteCommand(NoteRequest request, UUID studentId, UUID noteId);

    AssignBenefitCommand toAssignBenefitCommand(AssignBenefitRequest request, UUID studentId);
    @Mapping(target = "consumed", constant = "false")
    @Mapping(target = "consumedAt", ignore = true)
    BenefitResponse toBenefitResponse(AssignedBenefit assignedBenefit);
    List<BenefitResponse> toBenefitResponseList(List<BenefitEntry> benefitEntries);
}
