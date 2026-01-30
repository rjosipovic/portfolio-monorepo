package com.playground.challenge_manager.challenge.dataaccess.repositories;

import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity, UUID> {

    Optional<ChallengeEntity> findOneByIdAndUserId(UUID challengeId, UUID userId);
}
