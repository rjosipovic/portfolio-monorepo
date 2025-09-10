package com.playground.analytics_manager.dataaccess.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.UUID;

@Node("User")
@Data
@Builder
public class UserEntity {

    @Id
    private UUID id;

    @Property("alias")
    private String alias;
 }
