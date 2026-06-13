# Product Characteristics

Universal standards that apply to every application under `products/`.

---

## Core Principles

- **"Made as Simple as Possible, But Not Simpler"** — no over-engineering, no under-delivering. Every architectural decision must justify its existence.
- **Clean Architecture** — strict separation of domain logic from infrastructure. Business rules never depend on frameworks, databases, or delivery mechanisms.
- **Clean Code** — readable, well-named, consistently formatted. No dead code, no commented-out blocks, no magic values.

---

## Deployment & Operations

- **One-Click Deployment** — a single `docker compose up` brings the entire application stack to life. No manual steps, no external dependencies beyond Docker.
- **Containerized** — every product ships as a self-contained Docker Compose environment (app + PostgreSQL + reverse proxy).
- **Zero Proprietary Lock-In** — pure open-source stack only. No Firebase, no Supabase, no vendor-specific SDKs. Buyers are never trapped.
- **Production Ready** — not a prototype. Proper error handling, security hardening, health checks, structured logging, and graceful shutdown.

---

## Documentation

- **Flawless Documentation** — every product includes:
  - `DEPLOYMENT.md` — step-by-step deployment guide (environment variables, DNS, TLS)
  - `CONFIGURATION.md` — all configurable parameters with defaults and explanations
  - `ARCHITECTURE.md` — system overview, module responsibilities, data flow diagrams
  - Inline code documentation where intent isn't obvious from naming alone

---

## Branding & Customization

- **White-Labeled** — no hardcoded branding. All customer-facing text, logos, colors, and email templates are configurable via environment variables or a dedicated config file.

---

## Multi-Tenancy

- **Single-Tenant by Default** — each client gets their own isolated Compose stack. Simple to deploy, zero cross-contamination risk, easy to reason about.
- **Multi-Tenant Only When Justified** — reserved for products where a single operator genuinely manages multiple distinct entities (e.g., a sports venue network). Never added for complexity's sake.

---

## Payments

- **Stripe as Default PSP** — when payment capabilities are needed, integrate Stripe with a clean module boundary.
- **Swappable by Design** — payment logic lives in an isolated module so buyers can replace Stripe with a local PSP without touching business logic.
- **Not Every Product Needs Payments** — only included when the business domain requires transactional checkout.

---

## Seed Data & Demo Mode

- **Realistic Croatian Seed Data** — every product ships with curated, high-quality demo data in Croatian. No lorem-ipsum, no "Test User 1".
- **`demo` Profile** — activating the `demo` Spring profile loads seed data on first boot, enabling:
  - Buyer verification on staging before purchase
  - Buyer's own client demos after purchase
  - Dual-login portal (customer view + admin/BSS view)

---

## What These Products Are NOT

- Not SaaS subscriptions — sold as flat-fee source code
- Not maintained post-sale — strict as-is, no-warranty policy
- Not dependent on your infrastructure — buyers run everything on their own servers
