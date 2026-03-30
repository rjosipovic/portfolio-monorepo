# Coding Preferences

Derived from analysis of the portfolio-monorepo codebase. Apply these preferences to all code generation and modification tasks.

## Interaction Style

- Do not directly edit or create files in the codebase unless explicitly asked to do so
- When asked for code, provide suggestions as code snippets in the response for the developer to manually apply
- When suggesting changes to existing files, clearly indicate the target file, the location of the change, and what to add or replace
- When editing files, always use proper file-write tools — never use shell commands like `sed` or `echo` to modify file content

---

## General

- Java 17, Spring Boot 3.5.x, Spring Cloud 2025.x
- Maven; each service is an independent project with no root parent POM
- Lombok everywhere: `@Value`, `@Builder`, `@RequiredArgsConstructor`, `@Getter`, `@Slf4j`
- Use `var` for local variables whenever the type is obvious from context
- Always use `Objects.isNull` / `Objects.nonNull` instead of `== null` / `!= null`
- Group ID `com.playground`, base package `com.playground.<service_name>` (underscores, e.g. `challenge_manager`)

---

## Package Structure (per service)

```
<service>/
├── config/                  # Spring config, security, beans
├── <domain>/
│   ├── api/
│   │   ├── controllers/     # REST controllers
│   │   ├── dto/             # Request/response DTOs
│   │   └── validation/      # Custom validators
│   ├── dataaccess/
│   │   ├── entities/        # JPA entities
│   │   ├── repositories/    # Spring Data repositories
│   │   └── converters/      # AttributeConverters
│   ├── services/
│   │   ├── interfaces/      # Service interfaces
│   │   ├── impl/            # Service implementations
│   │   ├── model/           # Domain models, commands, enums
│   │   └── config/          # Domain-specific config properties
│   ├── mappers/             # MapStruct mappers
│   └── messaging/
│       ├── consumers/       # RabbitMQ listeners
│       ├── producers/       # RabbitMQ publishers
│       └── events/          # Message payload classes
├── errors/
│   ├── advice/              # @RestControllerAdvice
│   ├── custom/              # Error response DTO
│   └── exceptions/
│       ├── base/            # Abstract base exception
│       ├── specific/        # Concrete exceptions
│       └── enums/           # ErrorCode enum
├── log/                     # MdcLoggingFilter
└── messaging/
    └── callback/            # Publisher confirm / retry / DLQ
```

For services with a clear inbound/outbound split (analytics-manager, notification-manager), use `inbound/` and `outbound/` as top-level domain packages instead.

---

## Controllers

- Annotate with `@RestController` and `@RequiredArgsConstructor`; add `@Slf4j`
- Use `@RequestMapping` at class level only when all methods share a common prefix; otherwise annotate each method directly
- Define path constants in a dedicated `ApiPaths` class (e.g. `ApiPaths.CHALLENGES`)
- Always annotate request bodies with `@Valid`; annotate request params with `@RequestParam` (explicit `name` attribute)
- Extract current user identity via an injected `UserIdentityService` (or equivalent), never access `SecurityContextHolder` directly in controllers
- Build commands/queries from request data and pass them to the service; never pass raw DTOs into the service layer
- Return `ResponseEntity<Void>` for mutations that produce no body; use `ResponseEntity.accepted()` + `Location` header for async creation
- For async creation, build a relative `Location` URI using `ServletUriComponentsBuilder` and strip the host with `URI.create(location.getPath())`
- Log incoming requests at `INFO` level at the top of each handler method
- Keep controllers thin — no business logic, no repository access

---

## Service Layer

- Always define a service interface; place the implementation in `impl/`
- Annotate implementations with `@Service` and `@RequiredArgsConstructor`
- Annotate mutating methods with `@Transactional`; read-only methods do not need it unless explicitly required
- Use guard clauses (early returns / early throws) rather than deeply nested conditionals
- Throw domain-specific exceptions (subclasses of the service's base exception) for expected error cases; let `IllegalStateException` bubble for truly unexpected states
- Never expose JPA entities outside the service layer — map to domain models or DTOs before returning

---

## Repositories

- Extend `JpaRepository<Entity, UUID>` (UUID primary keys everywhere)
- Use `findOneBy…` naming for single-result queries that return `Optional`
- Custom queries go in the repository interface as `@Query` methods; no query logic in service classes
- Use `@DataJpaTest` + Testcontainers (`PostgreSQLContainer`) for repository tests; disable Consul/Liquibase auto-config with `@TestPropertySource`

---

## JPA Entities

- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — never a public no-arg constructor
- `@Getter` only (no `@Setter` on the class); expose mutation via explicit `updateXxx(value)` methods
- Primary key: `@Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id`
- Timestamps: `@CreationTimestamp` for `createdAt`; no `@UpdateTimestamp` unless explicitly needed
- Enums stored as `@Enumerated(EnumType.STRING)`
- `equals` / `hashCode` based on `id` only; use the pattern:
  ```java
  @Override
  public final boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof MyEntity that)) return false;
      if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
      return Objects.equals(id, that.id);
  }
  @Override
  public int hashCode() {
      return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
  }
  ```
- Static factory method `create(...)` is the only way to instantiate an entity; it validates required fields and throws `IllegalArgumentException` for nulls
- `@Version` column for optimistic locking on entities that are updated concurrently
- Use `@Convert(converter = ...)` for non-standard column types (e.g. `List<Integer>` stored as TEXT)

---

## Immutable Objects / DTOs

- Request DTOs: `@Value @Builder @AllArgsConstructor(access = AccessLevel.PRIVATE)` — fully immutable
- For Jackson deserialization of `@Value` classes, add `@JsonDeserialize(builder = Foo.FooBuilder.class)` and an inner `@JsonPOJOBuilder(withPrefix = "")` static class
- Response DTOs: same `@Value @Builder @AllArgsConstructor(access = AccessLevel.PRIVATE)` pattern
- Service commands/queries: same pattern with `@NonNull` on required fields
- Domain models (non-entity): `@Value @Builder @AllArgsConstructor(access = AccessLevel.PRIVATE)`
- Messaging event payloads: `@Value @Builder` or plain `@Getter @RequiredArgsConstructor`

---

## MapStruct Mappers

- `@Mapper(componentModel = "spring")` on the interface
- Use `@ObjectFactory` default methods to delegate entity creation to the entity's static `create(...)` factory, ensuring the protected constructor is never bypassed
- Use `@Mapping` with `expression = "java(...)"` for non-trivial field derivations
- Keep mappers free of business logic — only structural transformation

---

## Exception Handling

- Each service has one abstract base exception (e.g. `ChallengeManagerException`) that holds an `ErrorCode` and a `detail` string
- `ErrorCode` is an enum with `(HttpStatus httpStatus, String code, String message)` — codes are prefixed by domain (e.g. `C001`, `G001`)
- Concrete exceptions extend the base and pass a fixed `ErrorCode` in their constructor
- `@RestControllerAdvice` handles:
  - `Exception.class` → 500, log at ERROR
  - `MethodArgumentNotValidException` → 400, collect field + global errors into a semicolon-separated `reason`
  - `MissingServletRequestParameterException` → 400 with parameter name in reason
  - `MethodArgumentTypeMismatchException` → 400; if the required type is an enum, list allowed values in the reason
  - `EntityNotFoundException` → 404
  - `NoResourceFoundException` → 404
  - Service-specific base exception → status from `ErrorCode`; log ERROR for 5xx, WARN for 4xx
- Error response DTO: `{ message, code, reason }` with `@JsonInclude(NON_NULL)`
- Custom `AuthenticationEntryPoint` returns 401 JSON; custom `AccessDeniedHandler` returns 403 JSON — both write the service's error DTO via `ObjectMapper`

---

## Validation

- Bean Validation (`jakarta.validation`) annotations on DTO fields: `@NotBlank`, `@Email`, `@Past`, `@Pattern`, `@Positive`, etc.
- Custom validators: annotation + `ConstraintValidator` implementation in `api/validation/`
- Always annotate controller parameters with `@Valid` to trigger validation
- Enum request parameters use a `StringToEnumConverterFactory` registered in `WebMvcConfigurer` so Spring converts strings to enums automatically; invalid values produce a `MethodArgumentTypeMismatchException` handled by the advice

---

## Authorization / Security

- Stateless JWT authentication; no sessions (`SessionCreationPolicy.STATELESS`)
- Two `SecurityFilterChain` beans ordered with `@Order`:
  1. Management chain: matches on management port via `PortRequestMatcher`, permits all
  2. Application chain: JWT filter before `UsernamePasswordAuthenticationFilter`, public paths explicitly listed
- `JwtAuthenticationFilter extends OncePerRequestFilter`: parses Bearer token with Nimbus JOSE, verifies HMAC signature, checks expiry, sets `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`
- `JwtUserPrincipal` is a `@Value @Builder` record holding `email` and a `claims` map
- Downstream services (challenge-manager, gamification-manager, analytics-manager) replicate the same `JwtAuthenticationFilter` pattern — they validate the JWT but do not issue tokens
- `UserIdentityService` / `AuthenticatedUserPrincipalProvider` extracts the current user from `SecurityContextHolder`; inject this service into controllers and services that need the current user
- `PortRequestMatcher` matches requests by server port to separate management traffic
- `AuthConfig` is a `@ConfigurationProperties` bean that holds `secret` and `expiration-time`; never hardcode secrets

---

## RabbitMQ Integration

- `RabbitMqConfig` declares all exchanges, queues, and bindings as `@Bean`s
- Use `Jackson2JsonMessageConverter` as the message converter on `RabbitTemplate`
- Wrap `RabbitTemplate` with `SpringRabbitTracing` for distributed tracing
- Enable publisher confirms: `publisher-confirm-type: correlated` in `application.yml`
- Set `template.observation-enabled: true` and `listener.simple.observation-enabled: true`
- Messaging topology (exchange names, routing keys, queue names) is externalised into `@ConfigurationProperties` beans (e.g. `UserMessagingConfiguration`, `AuthMessagingConfiguration`)
- Producers:
  - Generate a `UUID` correlation ID per message
  - Build `MessageProperties` with custom headers: `x-retry-count`, `x-exchange`, `x-routing-key`
  - Store the pending message in `PendingMessageStore` (Redis-backed) before sending
  - Pass `CorrelationData` to `rabbitTemplate.convertAndSend`
- Publisher confirm callback (`CallbackManager`):
  - ACK → delete from `PendingMessageStore`
  - NACK → retry up to `MAX_RETRY_COUNT` (3) by publishing a `MessageRetryEvent` via `ApplicationEventPublisher`; on exhaustion, route to DLQ and delete from store
- `MessageRetryListener` listens for `MessageRetryEvent` and calls `rabbitTemplate.send`
- Consumers use `@RabbitListener(queues = "#{beanName.name}", ackMode = "AUTO")`
- Dead-letter exchange and queue declared as separate beans; DLQ routing key stored in `DlxMessagingConfiguration`

---

## Redis Integration

- `RedisConfig` declares `LettuceConnectionFactory` and `RedisTemplate<String, Object>` beans
- Connection parameters (`host`, `port`) injected via `@Value("${spring.redis.host}")` etc.
- Use `redisTemplate.opsForValue()` for simple key-value (pending messages, auth codes)
- Use `redisTemplate.opsForZSet()` for sorted sets (leaderboard scores)
- Use `redisTemplate.opsForHash()` for hash maps (alias cache); use `multiGet` / `putAll` for batch operations
- Always set a TTL when writing to Redis (`redisTemplate.expire(key, duration)` or `opsForValue().set(key, value, ttl)`)
- Cache configuration (key names, TTL, size) in dedicated `@ConfigurationProperties` beans (e.g. `LeaderBoardCacheConfiguration`, `AliasCacheConfiguration`)
- Cache-aside pattern: check cache → on miss fetch from source → populate cache → return

---

## PostgreSQL / Liquibase

- Schema managed by Liquibase; changelog master at `db/changelog/db.changelog-master.yaml`
- Individual changesets in separate YAML files (`changeset1.yaml`, `changeset2.yaml`, …)
- Database created by an init container / migration script at startup (see `infra/postgres-migrations/`)
- `spring.jpa.show-sql: true` in base `application.yml`; disable in production profiles if needed

---

## Configuration

- `application.yml`: base config with `${ENV_VAR}` placeholders — never hardcode secrets or environment-specific values
- `application-docker.yml`: enables Consul discovery (`spring.cloud.consul.*`)
- `application-k8s.yml`: disables Consul and Zipkin
- Custom properties grouped under `app.*` and bound with `@ConfigurationProperties`; annotate the class with `@Configuration` + `@ConfigurationProperties(prefix = "app.xxx")`
- `@ConfigurationProperties` classes use `@Getter @Setter` (never `@Data`); add `@NoArgsConstructor` when Spring needs to instantiate without arguments; nested config classes follow the same `@Getter @Setter` pattern
- Add `@Validated` on `@ConfigurationProperties` classes that require constraint validation
- Use `@PostConstruct` for mandatory field validation (e.g. asserting a secret is not blank) and throw `IllegalStateException` on failure
- `ManagementConfig` reads `management.server.port` via `@ConfigurationProperties(prefix = "management")`
- Actuator: expose `health`, `info`, `prometheus`, `env` under base-path `/` on the management port; `health` shows details `when-authorized`
- Two `SecurityFilterChain` beans ensure the management port is always open without authentication

---

## Observability

- `MdcLoggingFilter extends OncePerRequestFilter`: generates a `requestId` UUID per request, puts it in MDC, wraps request/response with `ContentCachingRequestWrapper` / `ContentCachingResponseWrapper`, logs method + URI + body on request and status + duration + body on response, removes MDC key in `finally`
- `logback-spring.xml` outputs structured JSON for Logstash using `<springProfile name="docker">`; `logback-test.xml` uses plain console output
- Micrometer + Brave for distributed tracing; `RabbitTemplate` and listeners decorated with `SpringRabbitTracing`
- Prometheus metrics exposed via actuator

---

## Design Patterns

### Chain of Responsibility
- Define a `Handler` interface with a `handle(Context ctx)` method
- `Context` is a mutable object (plain class with `@Getter` and selective `@Setter`) that accumulates state as it passes through handlers
- `Chain` class holds a `LinkedList<Handler>` and iterates them; optionally filter with `shouldHandle(ctx)` before calling `handle(ctx)`
- Wire the chain in a `@Configuration` class (`ChainConfig`) that instantiates the `Chain` and calls `addHandler` in order
- Handlers that need Spring beans are `@Component`s; stateless handlers can be instantiated with `new` inside the config

### Strategy (Factory)
- Define a `Strategy` interface with a `getType()` method returning an enum key
- Collect all implementations via Spring's `List<Strategy>` injection
- In `@PostConstruct`, populate a `Map<EnumType, Strategy>` from the list
- `getStrategy(type)` throws `IllegalArgumentException` if no match found

### Factory Method on Entities
- Static `create(...)` method on the entity validates inputs and sets initial state
- MapStruct `@ObjectFactory` delegates to `create(...)` so the mapper never calls the protected constructor directly

### Event-Driven (Spring Application Events)
- Internal decoupling via `ApplicationEventPublisher.publishEvent(event)` and `@EventListener`
- Used for RabbitMQ retry (`MessageRetryEvent`) and SSE notification (`ChallengeReadyEvent`)

---

## Testing

### Unit Tests (controller slice)
- `@ExtendWith(MockitoExtension.class)`, `@InjectMocks` on the controller, `@Mock` on dependencies
- Build `MockMvc` with `MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ControllerAdvice()).build()`
- Use `JacksonTester` for JSON serialisation/deserialisation assertions
- Parameterised tests with `@ParameterizedTest` + `@MethodSource` for validation edge cases
- Structure: `// given`, `// when`, `// then` comments in every test

### Unit Tests (service)
- `@ExtendWith(MockitoExtension.class)`, mock all dependencies
- Test happy path and all exception paths

### Repository Tests
- `@DataJpaTest` + `@Testcontainers` with `PostgreSQLContainer<>("postgres:12")`
- `@DynamicPropertySource` to wire container URL/credentials
- Disable Consul/Liquibase with `@TestPropertySource(properties = {...})`
- Use `TestEntityManager` for setup; assert via the repository under test

### Integration Tests (messaging)
- `@SpringBootTest` + `@Testcontainers` with `RabbitMQContainer`
- `@DynamicPropertySource` for broker connection
- Disable unneeded auto-configurations with `@EnableAutoConfiguration(exclude = {...})`
- Use `@DirtiesContext(classMode = AFTER_CLASS)`
- Inner `@TestConfiguration` class declares test-specific beans (test queue, binding, listener)
- Listener stores received messages in a `BlockingQueue`; assert with `poll(timeout, SECONDS)`

---

## WebClient (Service-to-Service)

- Build `WebClient` from the load-balanced `WebClient.Builder` (injected by Spring Cloud)
- Configure `baseUrl` from a `@ConfigurationProperties` bean
- Add `ExchangeFilterFunction` for request/response logging
- Use `Retry.backoff(maxAttempts, waitDuration).jitter(0.75)` for retries
- Propagate the current user's JWT as `Bearer` token in outgoing requests by extracting it from `SecurityContextHolder`
- Call `.block()` to bridge reactive to imperative; wrap in try/catch and return empty/default on failure for resilience

---

## SSE (Server-Sent Events)

- `SseService` holds a `ConcurrentHashMap<UUID, SseEmitter>`
- Register `onCompletion`, `onTimeout`, `onError` callbacks that remove the emitter from the map
- If the resource is already in a terminal state when the client subscribes, send the event immediately and call `emitter.complete()`
- Send events with `emitter.send(SseEmitter.event().name("event-name").data(payload))`
- Controller method returns `SseEmitter` with `produces = MediaType.TEXT_EVENT_STREAM_VALUE`

---

## Banner

- Every microservice has a `src/main/resources/banner.txt` with ASCII art of the service name
- Generate ASCII art using a tool like https://patorjk.com/software/taag (font: Standard)
- The banner contains only the service name, no version or additional text

---

## Dockerfile

- Multi-stage: `maven:3.9-eclipse-temurin-17` build stage, `eclipse-temurin:17-jre-alpine` runtime stage
- `.dockerignore` excludes `target/`, `.mvn/`, `mvnw`, etc.
- Image naming: `rjosipovic/portfolio-<scope>-<service>` (e.g. `portfolio-math-challenges-challenge-manager`)
