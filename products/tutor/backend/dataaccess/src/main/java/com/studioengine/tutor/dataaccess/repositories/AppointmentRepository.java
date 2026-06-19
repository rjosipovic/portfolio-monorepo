package com.studioengine.tutor.dataaccess.repositories;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByStripeSessionId(String stripeSessionId);

    Optional<Appointment> findByIdAndStripeSessionId(UUID id, String stripeSessionId);

    List<Appointment> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<Appointment> findByStateIn(Collection<AppointmentState> states);

    @Query("SELECT a FROM Appointment a WHERE a.state = 'PENDING_PAYMENT' AND a.stateChangedAt < :cutoff")
    List<Appointment> findOverduePendingPayments(OffsetDateTime cutoff);

    @Query("SELECT a FROM Appointment a JOIN a.timeSlot t WHERE a.state IN :states AND t.slotDate = :date ORDER BY t.startTime")
    List<Appointment> findByStatesAndSlotDate(Collection<AppointmentState> states, LocalDate date);

    @Query("SELECT a FROM Appointment a JOIN a.timeSlot t WHERE a.state IN :states AND t.slotDate BETWEEN :from AND :to ORDER BY t.slotDate, t.startTime")
    List<Appointment> findByStatesAndSlotDateBetween(Collection<AppointmentState> states, LocalDate from, LocalDate to);

    @Query("SELECT a FROM Appointment a JOIN a.timeSlot t WHERE a.state IN :states AND (t.slotDate < :date OR (t.slotDate = :date AND t.endTime <=:endTime))")
    List<Appointment> findUnclosedPastAppointments(Collection<AppointmentState> states, LocalDate date, java.time.LocalTime endTime);

    Optional<Appointment> findByTimeSlotIdAndStateIn(UUID timeSlotId, Collection<AppointmentState> states);
}
