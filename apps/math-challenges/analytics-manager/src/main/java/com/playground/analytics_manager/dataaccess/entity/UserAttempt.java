package com.playground.analytics_manager.dataaccess.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.ZonedDateTime;

@RelationshipProperties
@Data
@Builder
public class UserAttempt {

    @RelationshipId
    private String id;

    private ZonedDateTime attemptDate;
    private Integer resultAttempt;
    private Boolean correct;

    @TargetNode
    private UserEntity user;
}
