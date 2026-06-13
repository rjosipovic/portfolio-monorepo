package com.studioengine.tutor.dataaccess.repositories;

import com.studioengine.tutor.dataaccess.entities.StudentNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentNoteRepository extends JpaRepository<StudentNote, UUID> {

    List<StudentNote> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}