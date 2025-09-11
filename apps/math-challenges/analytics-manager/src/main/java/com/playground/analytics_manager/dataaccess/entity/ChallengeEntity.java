package com.playground.analytics_manager.dataaccess.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.UUID;

@Node("Challenge")
@Data
@Builder
public class ChallengeEntity {

    @Id
    private UUID id;

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
}
