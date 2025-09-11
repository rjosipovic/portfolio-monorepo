package com.playground.challenge_manager.challenge.dataaccess.repositories;

import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttemptEntity, UUID> {

    @Query("""
            SELECT a FROM challenge_attempts a
            WHERE a.userId = ?1
            ORDER BY a.attemptDate DESC
            LIMIT 10
            """)
    List<ChallengeAttemptEntity> findLast10AttemptsByUser(UUID userId);
}
