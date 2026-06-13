package com.studioengine.tutor.dataaccess.repositories;

import com.studioengine.tutor.dataaccess.entities.AppointmentStateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentStateLogRepository extends JpaRepository<AppointmentStateLog, UUID> {

    List<AppointmentStateLog> findByAppointmentIdOrderByTransitionedAtAsc(UUID appointmentId);
}
