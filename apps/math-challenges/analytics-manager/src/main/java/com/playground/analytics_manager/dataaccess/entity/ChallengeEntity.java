package com.playground.analytics_manager.dataaccess.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Objects;
import java.util.UUID;

@Node("Challenge")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Property("firstNumber")
    private int firstNumber;

    @Property("secondNumber")
    private int secondNumber;

    @Property("game")
    private String game;

    @Property("difficulty")
    private String difficulty;

    @Relationship(type = "ATTEMPT", direction = Relationship.Direction.INCOMING)
    private UserAttempt userAttempt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChallengeEntity that)) return false;
        if (this.id == null || that.id == null) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }

    public static ChallengeEntity create(UUID id, int firstNumber, int secondNumber, String game, String difficulty, UserAttempt userAttempt) {
        var challengeEntity = new ChallengeEntity();
        challengeEntity.setId(id);
        challengeEntity.setFirstNumber(firstNumber);
        challengeEntity.setSecondNumber(secondNumber);
        challengeEntity.setGame(game);
        challengeEntity.setDifficulty(difficulty);
        challengeEntity.setUserAttempt(userAttempt);
        return challengeEntity;
    }
}
