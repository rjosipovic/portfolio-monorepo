# Stack & Project Structure

## Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5.x (Java 17) — `honey-manager` |
| Database | PostgreSQL |
| Storefront | Vue 3 + Tailwind CSS |
| Operations App | Vue 3 + PrimeVue (mobile-first PWA) |
| Auth | Existing `user-manager` with role-based JWT |
| Infra | Docker, Helm, API Gateway routing |

## Project Structure

```
apps/honey-business/
├── honey-manager/          ← Spring Boot backend
├── honey-storefront/       ← Vue 3 + Tailwind (brand-heavy, customer-facing)
└── honey-operations/       ← Vue 3 + PrimeVue (utilitarian, mobile-first)
```

## Backend API Separation

Two controller layers within a single Spring Boot service, split by audience:

```
/api/v1/store/*        → Storefront (public/customer-facing)
/api/v1/operations/*   → Operations (HONEY_OPERATOR role required)
```

- Shared service layer — no logic duplication
- Different DTOs per audience (slim for store, rich for operations)
- Authorization enforced at SecurityFilterChain level by path prefix

## Auth Model

- Reuses existing `user-manager` for JWT issuance
- JWT claims include a `role` field: `CUSTOMER`, `HONEY_OPERATOR`
- `honey-manager` validates JWT and checks roles on operations endpoints
- Future: swap to Keycloak as identity provider without changing downstream role logic
