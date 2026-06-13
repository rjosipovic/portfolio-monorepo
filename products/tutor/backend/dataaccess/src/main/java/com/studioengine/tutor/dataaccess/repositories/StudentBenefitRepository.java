package com.studioengine.tutor.dataaccess.repositories;

import com.studioengine.tutor.dataaccess.entities.StudentBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentBenefitRepository extends JpaRepository<StudentBenefit, UUID> {

    @Query("SELECT b FROM StudentBenefit b WHERE b.student.id = :studentId AND b.consumed = false ORDER BY b.grantedAt ASC")
    Optional<StudentBenefit> findOldestUnconsumedByStudentId(UUID studentId);

    List<StudentBenefit> findByStudentIdOrderByGrantedAtDesc(UUID studentId);
}