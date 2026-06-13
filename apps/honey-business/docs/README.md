# samo. — Honey Business App Architecture

A small-business e-commerce platform for "samo." — a minimalist Croatian brand selling honey, tea, gingerbread, and future product lines. Part of the portfolio monorepo, designed for potential extraction into a standalone production app.

---

## Users

### Customers (Storefront)
- Browse products and bundles
- Place orders as guests (no account required)
- Track order status

### Business Operators (Operations App)
- Manage orders (confirm, ship, cancel)
- Manage inventory (stock levels, low-stock alerts)
- Manage products and bundles (CRUD)
- View sales summaries and reports

---

## Design Principles

- Build for MVP, don't lock ourselves into decisions that block future expansion
- One backend service — split only if complexity demands it
- Consistent with portfolio monorepo conventions (package structure, error handling, testing patterns)
- Designed for extraction into standalone production app when ready

---

## Documentation

| Document | Description |
|---|---|
| [Stack & Project Structure](stack.md) | Technology choices, project layout, auth model |
| [Domain Model & Database Schema](domain-and-schema.md) | Entities, relationships, ERD, Flyway migrations |
| [API Design & Package Structure](api-and-package-structure.md) | Store + Operations endpoints, Spring Boot layout |
| [Order State Machine](order-state-machine.md) | State diagram, transitions, customer flow |
| [Messaging](messaging.md) | RabbitMQ events, topology, notification integration |
| [Image Storage](image-storage.md) | S3/MinIO upload/serving strategy |
