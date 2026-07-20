package com.studioengine.tutor.benefit;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.Student;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BenefitService {

    AssignedBenefit assign(AssignBenefitCommand command);

    BenefitApplication apply(Student student, BigDecimal originalPrice);

    void consume(BenefitApplication application, Appointment appointment);

    List<BenefitEntry> getBenefits(UUID studentId);
}
