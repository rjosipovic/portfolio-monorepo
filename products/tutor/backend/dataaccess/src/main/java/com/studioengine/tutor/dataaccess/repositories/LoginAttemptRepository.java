package com.studioengine.tutor.dataaccess.repositories;


import com.studioengine.tutor.dataaccess.entities.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    @Query("SELECT COUNT(l) FROM LoginAttempt l WHERE l.email = :email AND l.successful = false AND l.attemptedAt > :since")
    long countFailedAttemptsSince(String email, OffsetDateTime since);
}
