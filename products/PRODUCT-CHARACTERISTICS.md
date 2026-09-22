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

- **White-Labeled** — no hardcoded branding. All customer-facing text, logos, colors, and email
  templates are configurable via environment variables or a dedicated config file.
- **Agency Re-Themes the Existing Storefront** — white-label means the shipped storefront stays;
  the agency re-themes it (branding, colors, styling, copy) per end client on the same codebase.
  It is not a headless API for agencies to build a replacement frontend against.

---

## Multi-Tenancy

- **Single-Tenant by Default** — each client gets their own isolated Compose stack. Simple to deploy, zero cross-contamination risk, easy to reason about.
- **Multi-Tenant Only When Justified** — reserved for products where a single operator genuinely manages multiple distinct entities (e.g., a sports venue network). Never added for complexity's sake.
- **Multi-Tenant SaaS Edition — Deferred (Not Planned)** — an agency could prefer one shared
  multi-tenant instance (a SaaS serving many end clients) over a fleet of single-tenant stacks.
  This is explicitly deferred: it lands directly on the money-critical booking/payment domain
  (tenant-scoping every query, per-tenant runtime theming, isolation guarantees), contradicts the
  single-tenant default, and is currently speculative with no customer demand. Revisit only when a
  paying agency's operational pain proves the need, and then only as a separate premium edition
  (e.g. "tutor Cloud") — never by converting the base single-tenant product. The agency can already
  run a SaaS *business model* on single-tenant instances without this.

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

## Common Capability Set

Every product must implement the following capability set. These are the cross-cutting
capabilities that make a purpose-built product win against a WordPress-plus-plugins stack:
a designed system for a specific business, correct where money is on the line, and easy for a
non-technical end user to benefit from — with just enough content editing that no separate CMS
is ever needed.

**Buyer model.** The buyer is a **technical digital agency**, not the end business. The agency
buys the system once and resells many instances to its own end clients, re-theming the storefront
per client (see White-Label). The agency hosts and operates the instances (how they host is their
decision) and is responsible for serving their non-technical end users. Your responsibility stops
at delivering a tool that gives the end user real benefit at their original profession with minimal
effort — because if the tool is low-value or high-effort, the agency cannot resell it.

Each planned product (`tutor`, `barber-shop`, `rent-a-car`, `samo-shop`, `termini`,
`kids-activities`) instantiates these capabilities for its own vertical. The list below is
the shared baseline, not per-app features.

### 1. Self-Editable Content Layer

- **End-user-managed storefront** — the public-facing pages (landing, services, prices, hours,
  gallery, about, contact) are editable by the non-technical end user through the storefront the
  agency has re-themed for them. This neutralizes WordPress's single biggest advantage.
- **Low-effort editing** — the end user updates their own content without a developer and without
  a learning curve; editing is a built-in, obvious part of the app.
- **Opinionated, not open-ended** — cover the 80% of content a small business actually needs;
  do not attempt to out-CMS WordPress with arbitrary page building.
- **Content lives in the product** — no bolted-on separate CMS; content is a first-class,
  white-labeled part of the app.

### 2. Money-Critical Correctness

- **Bulletproof core domain** — the flows where the business loses money if they break
  (booking/scheduling, availability, payments, invoicing) are designed as one coherent domain,
  not stitched from independent plugins.
- **No double-booking / no lost slots** — availability and reservation logic is transactionally
  correct under concurrency.
- **Reliable payment path** — payment capture and webhook handling are idempotent and reconciled;
  a failed webhook never silently loses a paid order.
- **Compliant invoicing** — invoices are generated correctly for the target market (e.g. Croatian
  HUB3 payment slips) and reconcile against payments.
- **Proven by tests** — money-critical paths carry the strongest test coverage in the product.

### 3. Deployment & Ownership Story

- **Technical buyer, `docker compose up` is sufficient** — the buyer is a technical agency, so a
  single-command stack plus flawless documentation is enough. No consumer-grade installer or
  managed-hosting offering is required from you.
- **Designed for fleet deployment** — the product is built to run as many isolated single-tenant
  instances, one per end client, operated by the agency (see Multi-Tenancy). The agency chooses
  where and how to host.
- **Straightforward per-client bring-up** — standing up a new instance for a new end client is a
  repeatable, documented operation: configure environment, re-theme, deploy.

### 4. Vertical Pre-Configuration

- **Sold as "the <business> system", not "a booking app"** — each product ships pre-configured for
  its exact vertical: the right fields, flow, terminology, and localized invoicing built in.
- **Tailored demo** — the `demo` profile presents that vertical convincingly (see Seed Data & Demo
  Mode), so a prospect immediately recognizes their own business.

### 5. Recurring Value (Optional, Opt-In)

- **No SaaS rent by default** — this does not override the flat-fee, own-the-source, as-is model
  below. It is strictly optional and opt-in.
- **Optional paid services** — support, updates, hosting, and new features may be offered as
  separate opt-in engagements for buyers who want them.
- **Never lock-in** — declining any optional service must leave the buyer with a fully functional,
  self-hostable product they already own.

### 6. End-User Value with Minimal Effort

- **Makes the end user better at their profession** — the product's purpose is to help the end
  user (tutor, barber, rental operator, shop owner) do their actual job better, not to hand them
  administrative overhead.
- **Low effort to adopt and operate** — everyday use requires no technical skill and no steep
  learning curve; the common tasks are fast and obvious.
- **Value is the resale enabler** — a low-value or high-effort tool cannot be resold by the agency,
  so end-user benefit is a first-class product requirement, not a nice-to-have.

---

## What These Products Are NOT

- Not SaaS subscriptions — sold as flat-fee source code
- Not maintained post-sale by default — strict as-is, no-warranty policy (optional paid
  support/updates are a separate opt-in engagement, never a dependency)
- Not dependent on your infrastructure — buyers run everything on their own servers
- Not a CMS — content editing covers the small-business essentials, not arbitrary page building
