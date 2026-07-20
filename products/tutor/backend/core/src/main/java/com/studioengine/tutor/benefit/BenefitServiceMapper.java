package com.studioengine.tutor.benefit;

import com.studioengine.tutor.dataaccess.entities.StudentBenefit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BenefitServiceMapper {

    @Mapping(source = "student.id", target = "studentId")
    AssignedBenefit toAssignedBenefit(StudentBenefit studentBenefit);

    List<BenefitEntry> toBenefitEntries(List<StudentBenefit> studentBenefit);

}
