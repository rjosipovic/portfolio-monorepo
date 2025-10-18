package com.playground.analytics_manager.config;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This central configuration class is responsible for enabling and creating all
 * ConfigurationProperties beans for the application. By placing the annotation here,
 * we ensure these beans are created early in the application lifecycle and are available
 * for any other component that needs them.
 */
@Configuration
@EnableConfigurationProperties({Neo4jProperties.class, LiquibaseProperties.class})
public class AppPropertiesConfig {
}
