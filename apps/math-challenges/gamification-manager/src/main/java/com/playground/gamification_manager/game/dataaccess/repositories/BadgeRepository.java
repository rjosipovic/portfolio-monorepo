package com.playground.gamification_manager.game.dataaccess.repositories;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BadgeRepository extends CrudRepository<BadgeEntity, UUID> {

    @Query("SELECT b FROM badges b WHERE b.userId = :userId")
    List<BadgeEntity> findAllByUserId(@Param("userId") UUID userId);
}
