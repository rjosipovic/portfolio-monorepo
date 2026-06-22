package com.studioengine.tutor.dataaccess.repositories;

import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    List<TimeSlot> findByStateAndSlotDateGreaterThanEqual(TimeSlotState state, LocalDate fromDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.id = :id")
    Optional<TimeSlot> findByIdForUpdate(UUID id);

    @Query("SELECT t FROM TimeSlot t WHERE t.state = 'RESERVED' AND t.stateChangedAt < :cutoff")
    List<TimeSlot> findExpiredReservations(OffsetDateTime cutoff);

    List<TimeSlot> findBySlotDateBetween(LocalDate from, LocalDate to);

    List<TimeSlot> findBySlotDateBetweenAndState(LocalDate from, LocalDate to, TimeSlotState state);

    boolean existsBySlotDateAndStartTime(LocalDate date, LocalTime startTime);
}

