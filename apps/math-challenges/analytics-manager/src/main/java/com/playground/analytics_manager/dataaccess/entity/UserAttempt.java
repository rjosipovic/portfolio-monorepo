package com.playground.analytics_manager.dataaccess.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.ZonedDateTime;
import java.util.Objects;

@RelationshipProperties
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAttempt {

    @RelationshipId
    private String id;

    private ZonedDateTime attemptDate;
    private Integer resultAttempt;
    private Boolean correct;

    @TargetNode
    private UserEntity user;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAttempt that)) return false;
        if (this.id == null || that.id == null) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? id.hashCode() : getClass().hashCode();
    }

    public static UserAttempt create(ZonedDateTime attemptDate, Integer resultAttempt, Boolean correct, UserEntity user) {
        var userAttempt = new UserAttempt();
        userAttempt.setAttemptDate(attemptDate);
        userAttempt.setResultAttempt(resultAttempt);
        userAttempt.setCorrect(correct);
        userAttempt.setUser(user);
        return userAttempt;
    }
}
