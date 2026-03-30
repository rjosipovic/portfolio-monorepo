# Project Structure

## Top-Level Layout

```
portfolio-monorepo/
├── apps/               # Business domain microservices
├── services/           # Shared/cross-cutting microservices
├── infra/              # Infrastructure as Code
├── .env.template       # Required environment variables template
├── PORT-ALLOCATION.md  # Port assignment reference
└── GEMINI-*.md         # AI assistant guidelines (legacy)
```

## apps/ — Domain Services

```
apps/
├── math-challenges/
│   ├── challenge-manager/      # Challenge CRUD, game logic, scoring
│   ├── gamification-manager/   # Leaderboards, user progression
│   ├── analytics-manager/      # Analytics & statistics (Neo4j)
│   └── fe/                     # Vanilla JS frontend
├── movie-hub/
│   ├── movie-hub-be/           # Spring Boot + Neo4j backend
│   └── movie-hub-fe/           # Frontend
└── portfolio-web/              # Static portfolio site
```

## services/ — Shared Services

```
services/
├── api-gateway/            # Spring Cloud Gateway (sole client entry point)
├── user-manager/           # Auth, JWT, user accounts
└── notification-manager/   # Email delivery
```

## infra/ — Infrastructure

```
infra/
├── docker-compose.yml          # Full local stack
├── postgres-migrations/        # DB schema bootstrap (runs on startup)
├── rabbitmq/                   # Broker config & definitions
├── consul/                     # Service discovery config
├── prometheus/                 # Scrape configs
├── grafana/                    # Dashboards
├── logstash/                   # Log pipeline
├── kibana/                     # Log viewer config
├── alertmanager/               # Alert rules
├── k8s/charts/portfolio/       # Helm umbrella chart
│   ├── values.yaml             # All service config & feature flags
│   └── templates/
│       ├── infrastructure/     # DB/broker deployments
│       ├── apps/               # Microservice deployments
│       └── configs/            # ConfigMaps & Secrets
├── build-portfolio.sh
├── publish-portfolio.sh
├── run-portfolio.sh
├── run-local-infra.sh
└── deploy-k8s.sh
```

## Per-Service Layout (Spring Boot)

Each microservice follows the same internal structure:

```
<service>/
├── src/main/java/com/playground/<service_name>/
│   ├── <ServiceApplication>.java
│   ├── config/             # Spring config classes, security, beans
│   ├── <domain>/           # Feature package (e.g. challenge/, user/)
│   │   ├── api/            # REST controllers + request/response DTOs
│   │   ├── dataaccess/     # JPA entities + repositories
│   │   ├── services/       # Business logic
│   │   ├── mappers/        # MapStruct mappers
│   │   └── messaging/      # RabbitMQ listeners/publishers
│   ├── errors/             # Exception classes + global error handler
│   ├── log/                # MDC logging filter
│   └── messaging/          # Shared messaging config/utilities
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   ├── application-k8s.yml
│   ├── logback-spring.xml
│   └── db/changelog/       # Liquibase migration files (PostgreSQL services)
├── src/test/
├── Dockerfile
└── pom.xml
```

## Conventions

- **Group ID**: `com.playground` across all services
- **Base package**: `com.playground.<service_name>` (underscores, e.g. `challenge_manager`)
- **Docker image naming**: `rjosipovic/portfolio-<scope>-<service>` (e.g. `portfolio-math-challenges-challenge-manager`)
- **New Helm service**: add entry to `infra/k8s/charts/portfolio/values.yaml` and create a template under `templates/apps/`
- **Never commit secrets**: use `${ENV_VAR}` placeholders in `application.yml`; supply values via `.env`, IntelliJ run configs, or K8s Secrets
