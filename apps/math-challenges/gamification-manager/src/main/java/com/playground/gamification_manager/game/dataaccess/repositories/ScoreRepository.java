package com.playground.gamification_manager.game.dataaccess.repositories;

import com.playground.gamification_manager.game.dataaccess.domain.ScoreEntity;
import com.playground.gamification_manager.game.dataaccess.domain.UserScore;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreRepository extends CrudRepository<ScoreEntity, UUID> {

    @Query("SELECT SUM(s.score) FROM scores s WHERE s.userId = :userId")
    Integer totalScoreByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT new com.playground.gamification_manager.game.dataaccess.domain.UserScore(s.userId, SUM(s.score))
        FROM scores s
        GROUP BY s.userId
        ORDER BY SUM(s.score) DESC
        """)
    List<UserScore> totalScorePerUser();
}
