# Tutor Engine — Requirements Specification

**Project Type:** Standalone Multi-Module Spring Boot Application (Commercial White-Label Asset)  
**Target Deployment:** Single-Tenant Docker Compose (App + PostgreSQL)  
**Location:** `products/tutor/`

---

## 1. System Overview

A white-label, single-provider appointment scheduling and Business Support System (BSS). One codebase adapts to different service niches (math tutoring, piano lessons, etc.) solely via database seed profiles and environment configuration.

### White-Label Proof on Staging

Two separate running instances of the same backend + frontend code, deployed side-by-side:

| Instance | URL | Database | Seed Context | Theme |
|----------|-----|----------|--------------|-------|
| Math Studio | `tutor-math.roman-josipovic.from.hr` | `tutor_math_db` | `math` | Geometric / Academic |
| Piano Studio | `lessons-piano.roman-josipovic.from.hr` | `tutor_piano_db` | `piano` | Elegant / Artistic |

---

## 2. Technical Stack

- **Java 21, Spring Boot 3.5.x**
- **Group ID:** `com.studioengine`
- **Base Package:** `com.studioengine.tutor`
- **Database:** PostgreSQL (single-tenant isolated DB per instance)
- **Migrations:** Liquibase with contextual seed data
- **PDF Generation:** Apache PDFBox (open-source, no AGPL concerns)
- **QR/Barcode:** ZXing (HUB3 2D barcode for Croatian bank transfers)
- **Calendar:** iCal4j (`.ics` file generation)
- **Payments:** Stripe (webhook with signature verification)
- **Email:** Spring JavaMailSender
- **Deployment:** Standalone Dockerfile + Docker Compose (no Consul, RabbitMQ, or ELK dependencies)

---

## 3. Project Structure

```
products/tutor/
├── tutor-backend/
│   ├── pom.xml                   # Parent POM (module list)
│   ├── tutor-app/                # Spring Boot main, config, bean wiring
│   ├── tutor-api/                # Controllers, DTOs, validation, security filters
│   ├── tutor-core/               # Business logic, mail services, PDF generation, MapStruct
│   └── tutor-dataaccess/         # JPA entities, repositories, Liquibase changelogs
├── tutor-storefront/             # Public client UI (themed via config per instance)
├── tutor-dashboard/              # Admin BSS panel (shared across instances)
├── Dockerfile
└── docker-compose.yml            # Multi-instance composition (math + piano)
```

---

## 4. Database Schema

```
[ServiceCategory] 1 ── * [TimeSlot]
                             │
                             1 ── * [Appointment] * ── 1 [Student]
                                         │
                                         1 ── 1 [Transaction]
```

### Liquibase Context Strategy

```yaml
# db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - include:
      file: db/changelog/schema-bootstrap.yaml       # Always runs
  - include:
      file: db/changelog/seed-math-data.yaml
      context: math
  - include:
      file: db/changelog/seed-piano-data.yaml
      context: piano
```

- **`math` context:** "Primary School Algebra", "Državna Matura Prep", "College Calculus"
- **`piano` context:** "Beginner Scales", "Classical Repertoire", "Music Theory"

---

## 5. Functional Requirements

### Module A: Public Storefront (tutor-storefront)

No client registration or login required. Identity verified via email during checkout.

| ID | Requirement | Details |
|----|-------------|---------|
| RF-01 | Dynamic Service Discovery | `GET /api/v1/public/services` returns active lesson options, durations, pricing for the instance |
| RF-02 | Real-Time Availability Grid | Visual matrix of open 45/60-minute blocks for the next 14 rolling days. Pessimistic locking (`SELECT FOR UPDATE`) on slot reservation to prevent double-booking |
| RF-03 | Guest Checkout | Collects: name, email, phone, session notes (e.g., "Struggling with quadratic functions") |
| RF-04 | Hybrid Payment Routing | **Stripe:** webhook confirms payment → appointment state = `PAID`. **Bank Transfer:** generates PDF with HUB3 2D barcode → state = `PENDING_PAYMENT` awaiting manual confirmation |

### Module B: Admin Dashboard (tutor-dashboard)

Secure, authenticated panel for the instructor.

| ID | Requirement | Details |
|----|-------------|---------|
| RF-05 | Master Calendar | All scheduled slots with actions: cancel, reschedule |
| RF-06 | Availability Template Matrix | Set baseline weekly shifts (e.g., Tue 15:00–21:00) + calendar overrides (vacation, sick days) |
| RF-07 | Student CRM Ledger | Booking history, transaction metrics, internal progress notes per student |
| RF-08 | Financial Ledger | Monthly revenue, billable hours, manual bank transfer confirmation (`PENDING_PAYMENT` → `CONFIRMED`) |

---

## 6. Automated Communications

All handled via Spring `JavaMailSender` + `@Scheduled` tasks.

| ID | Trigger | Action |
|----|---------|--------|
| AC-01 | Purchase confirmed | Email with dynamic invoice PDF + `.ics` calendar invitation |
| AC-02 | Daily at midnight | Scan appointments in next 24h, send reminder emails |
| AC-03 | 2 hours after lesson ends | Thank-you email with progress comments + fast-track rebooking link |

---

## 7. Key Technical Decisions

| Decision | Rationale |
|----------|-----------|
| Pessimistic locking over optimistic | Short contention window, high cost of conflict (double-booking). Single instance makes this simple and bulletproof |
| PDFBox over iText | Fully open-source (Apache 2.0). No AGPL licensing risk for commercial resale |
| Single storefront + config-driven theming | Proves white-label capability without code duplication. CSS variables, logo URL, copy text all from env/config |
| Stripe webhook with signature verification | Standard `Stripe-Signature` header validation. No polling |
| No multi-tenancy | Single-tenant by design. Each buyer deploys their own isolated stack |

---

## 8. Appointment State Machine

```
AVAILABLE → RESERVED → PAID → COMPLETED
                    ↘ PENDING_PAYMENT → CONFIRMED → COMPLETED
                                      ↘ EXPIRED (auto after 48h)
            Any state → CANCELLED
```

---

## 9. Configuration-Driven White Labeling

All instance identity is externalized:

```yaml
# Environment variables per instance
APP_BRAND_NAME=Math Studio
APP_BRAND_LOGO_URL=https://...
APP_BRAND_PRIMARY_COLOR=#2563eb
APP_BRAND_LOCALE=hr
APP_BRAND_CURRENCY=EUR
APP_BRAND_TIMEZONE=Europe/Zagreb
SPRING_LIQUIBASE_CONTEXTS=math
```

The storefront reads these at build/runtime to apply theming. The backend serves them via a public config endpoint for dynamic frontends.
