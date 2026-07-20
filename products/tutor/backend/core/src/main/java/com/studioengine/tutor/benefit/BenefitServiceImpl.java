package com.studioengine.tutor.benefit;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.StudentBenefit;
import com.studioengine.tutor.dataaccess.repositories.StudentBenefitRepository;
import com.studioengine.tutor.dataaccess.repositories.StudentRepository;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BenefitServiceImpl implements BenefitService {

    private final StudentBenefitRepository studentBenefitRepository;
    private final StudentRepository studentRepository;
    private final BenefitServiceMapper benefitServiceMapper;

    @Override
    public AssignedBenefit assign(AssignBenefitCommand command) {
        var student = findStudent(command.getStudentId());
        var benefit = StudentBenefit.create(
                student,
                command.getType(),
                command.getValue(),
                command.getNote()
        );
        var savedBenefit = studentBenefitRepository.save(benefit);
        return benefitServiceMapper.toAssignedBenefit(savedBenefit);
    }

    @Override
    public BenefitApplication apply(Student student, BigDecimal originalPrice) {

        return studentBenefitRepository.findOldestUnconsumedByStudentId(student.getId())
                .map(benefit -> {
                    var finalPrice = BenefitCalculator.calculateFinalPrice(originalPrice, benefit.getType(), benefit.getValue());
                    return BenefitApplication.builder()
                            .benefitId(benefit.getId())
                            .type(benefit.getType())
                            .originalPrice(originalPrice)
                            .finalPrice(finalPrice)
                            .applied(true)
                            .build();
                })
                .orElse(BenefitApplication.none(originalPrice));
    }

    @Override
    public void consume(BenefitApplication application, Appointment appointment) {
        if (!application.isApplied()) return;

        var benefit = studentBenefitRepository.findById(application.getBenefitId())
                .orElseThrow(() -> new IllegalStateException("Benefit %s not found during consumption".formatted(application.getBenefitId())));
        benefit.consume(appointment);
        studentBenefitRepository.save(benefit);
    }

    @Override
    public List<BenefitEntry> getBenefits(UUID studentId) {
        var studentBenefits = studentBenefitRepository.findByStudentIdOrderByGrantedAtDesc(studentId);
        return benefitServiceMapper.toBenefitEntries(studentBenefits);
    }

    private Student findStudent(UUID studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
    }
}
