# Tech Stack

## Core

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.6
- **Spring Cloud**: 2025.0.0 (Gateway, Consul Discovery)
- **Build Tool**: Maven (each service is an independent Maven project, no root parent POM)

## Key Libraries

- **Lombok**: Boilerplate reduction (`@Builder`, `@Value`, `@Data`, etc.)
- **MapStruct 1.5.5**: DTO ↔ entity mapping
- **Nimbus JOSE + JWT**: JWT creation and validation
- **Liquibase**: PostgreSQL schema migrations (`db/changelog/db.changelog-master.yaml`)
- **Testcontainers**: Integration tests (PostgreSQL, RabbitMQ)
- **JaCoCo**: Code coverage (excludes DTOs, configs, exceptions)

## Databases & Middleware

| Service | Storage |
|---|---|
| user-manager | PostgreSQL + Redis (cache) |
| challenge-manager | PostgreSQL + Redis |
| gamification-manager | PostgreSQL |
| analytics-manager | Neo4j |
| notification-manager | — |

- **RabbitMQ 3.13**: Async messaging between services
- **Redis**: Caching and short-lived token storage
- **PostgreSQL 12**: Relational data
- **Neo4j 2025.09.0**: Graph analytics
- **MailHog**: Local email capture (used by notification-manager in development)

## Observability

- **Micrometer + Brave**: Distributed tracing
- **Zipkin**: Trace aggregation (disabled in K8s)
- **Prometheus + Grafana**: Metrics
- **Logstash + Kibana**: Log aggregation (structured JSON via `logback-spring.xml`)
- **MDC**: `requestId` injected into all log lines

## Infrastructure

- **Docker / Docker Compose**: Local development
- **Kubernetes + Helm**: Production (umbrella chart at `infra/k8s/charts/portfolio/`)
- **Consul**: Service discovery for local/Docker only (disabled in K8s, replaced by native DNS)

## Frontend

- Vanilla JavaScript, HTML5, CSS3 (no framework)

---

## Common Commands

Each service is built and run independently from its own directory.

### Build a service
```bash
cd apps/math-challenges/challenge-manager
./mvnw clean package -DskipTests
```

### Run tests for a service
```bash
./mvnw test
```

### Run full local stack (Docker Compose)
```bash
./infra/run-portfolio.sh
```

### Run infrastructure only (DBs, brokers, etc.)
```bash
./infra/run-local-infra.sh
```

### Build all Docker images
```bash
./infra/build-portfolio.sh
```

### Publish images to registry
```bash
./infra/publish-portfolio.sh
```

### Deploy to Kubernetes
```bash
./infra/deploy-k8s.sh
```

---

## Configuration Strategy

Spring Profiles control environment differences:

| Profile | Environment | Discovery |
|---|---|---|
| `docker` | Local IntelliJ run configs | Consul (via `localhost`) |
| `docker` | Docker Compose | Consul (internal) |
| `k8s` | Kubernetes | Native K8s DNS |

- `application.yml`: Base config with `${ENV_VAR}` placeholders
- `application-docker.yml`: Enables Consul discovery
- `application-k8s.yml`: Disables Consul and Zipkin

All secrets are injected via environment variables — never hardcoded. See `.env.template` for required variables.

## Port Allocation (local/hybrid mode)

In Docker Compose each service runs in its own container on standard ports, so no port configuration is needed. When running services directly on the **host machine** (Hybrid Mode — e.g. IntelliJ run configs alongside a Docker infra stack), configure ports via environment variables:

| Service | SERVICE_PORT | MANAGEMENT_PORT |
|---|---|---|
| api-gateway | 8080 | 9080 |
| user-manager | 8180 | 9180 |
| challenge-manager | 8181 | 9181 |
| gamification-manager | 8182 | 9182 |
| analytics-manager | 8183 | 9183 |
| notification-manager | 8280 | 9280 |
| frontend | 3000 | — |
