package com.studioengine.tutor.dataaccess.repositories;

import com.studioengine.tutor.dataaccess.entities.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRecordRepository extends JpaRepository<OtpRecord, UUID> {

    Optional<OtpRecord> findByEmailAndUsedFalseAndExpiresAtAfter(String email, OffsetDateTime now);

    @Modifying
    @Query("UPDATE OtpRecord o SET o.used = true WHERE o.email = :email AND o.used = false")
    void invalidateAllByEmail(String email);
}

