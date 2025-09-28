package com.playground.analytics_manager.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.sql.DriverManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LiquibaseConfig {

    private final Neo4jProperties neo4jProperties;
    private final LiquibaseProperties liquibaseProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void runLiquibase() {
        if (!liquibaseProperties.isEnabled()) {
            log.info("Liquibase is disabled.");
            return;
        }

        var boltUrl = neo4jProperties.getUri().toString();
        // The liquibase-neo4j extension requires a JDBC-style URL.
        var jdbcUrl = "jdbc:neo4j:" + boltUrl;
        var user = neo4jProperties.getAuthentication().getUsername();
        var password = neo4jProperties.getAuthentication().getPassword();
        var changeLog = liquibaseProperties.getChangeLog();

        log.info("Starting Liquibase for Neo4j.");
        log.debug("Liquibase Neo4j JDBC URL: {}", jdbcUrl);
        log.debug("Liquibase Changelog: {}", changeLog);

        try {
            // Manually register the Neo4j JDBC driver from the neo4j-jdbc-driver dependency
            Class.forName("org.neo4j.jdbc.Driver");

            // Use try-with-resources for automatic resource management
            try (var connection = DriverManager.getConnection(jdbcUrl, user, password)) {
                var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                var liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);

                liquibase.update(new Contexts(), new LabelExpression());
                log.info("Liquibase for Neo4j has run successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to run Liquibase for Neo4j.", e);
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }
}
