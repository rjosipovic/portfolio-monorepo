package com.studioengine.tutor.dataaccess.repositories;


import com.studioengine.tutor.dataaccess.entities.CancellationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CancellationTokenRepository extends JpaRepository<CancellationToken, UUID> {

    Optional<CancellationToken> findByToken(String token);
}

