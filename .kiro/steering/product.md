# Product Overview

This is a **portfolio monorepo** showcasing enterprise-grade microservices architecture across multiple business domains.

## Domains

- **Math Challenges** (`apps/math-challenges/`): The primary domain. A gamified math challenge platform where users solve arithmetic problems, earn scores, and compete on leaderboards. Includes challenge generation, gamification/leaderboards, analytics, and a vanilla JS frontend.
- **Movie Hub** (`apps/movie-hub/`): A movie database/discovery app backed by Neo4j.
- **Portfolio Web** (`apps/portfolio-web/`): A static personal portfolio site.

## Shared Services

- **api-gateway**: Single entry point for all client traffic (Spring Cloud Gateway).
- **user-manager**: Authentication, JWT issuance, and user account management.
- **notification-manager**: Email notification delivery.

## Purpose

The project is a developer portfolio demonstrating: microservices decomposition, event-driven communication, multi-database strategies, containerization, and Kubernetes deployment.
