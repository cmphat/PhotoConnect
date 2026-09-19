# PhotoConnect Backend Plan — Final State

This file records the implemented backend scope after feature freeze. Checked items are present in source and automated tests; manual browser status is tracked separately in `docs/final/project-status.md`.

## Foundation

- [x] Spring Boot 3.3 / Java 21 target / Maven WAR
- [x] SQL Server datasource and Spring Data JPA/Hibernate
- [x] JSP view resolution and `open-in-view=false`
- [x] Central MVC/JSON exception handling and standard error codes

## Authentication and accounts

- [x] `User` entity/repository
- [x] Registration validation, unique email, BCrypt hash
- [x] Session login/logout and session-ID rotation
- [x] `CUSTOMER`, `PHOTOGRAPHER`, `ADMIN` role checks
- [x] Active/inactive/banned account enforcement
- [x] Admin user search/filter/status management with self-protection

The final authentication architecture is server-side HTTP session authentication. JWT and a Spring Security filter-chain login are not implemented or required by the frozen scope; `spring-security-crypto` supplies BCrypt.

## Photographer marketplace

- [x] Photographer profile and verification lifecycle
- [x] Onboarding with one profile per user
- [x] Admin approve/reject for pending applications
- [x] Approved-only public list/detail
- [x] Keyword/city/price/experience search
- [x] Database pagination
- [x] Cloudinary portfolio upload/owner deletion
- [x] Blocked-date availability management

## Booking and payment

- [x] Booking entity and server-owned price/customer fields
- [x] Future/availability/self-booking checks
- [x] Customer and photographer ownership views
- [x] Accept/reject/cancel/complete state machine
- [x] One-to-one deposit with server-calculated 30% amount
- [x] Demo QR/card state flow, locking, idempotency, result, receipt
- [x] Legacy deposit compatibility

The payment implementation is local simulation only; no production gateway or real transfer exists.

## Chat and reviews

- [x] Persistent booking messages
- [x] STOMP/SockJS plus REST fallback
- [x] Session-derived participant validation and explicit origin allowlist
- [x] Completed-booking review eligibility and uniqueness
- [x] Visible-review rating aggregation
- [x] Admin hide/unhide moderation

## Hardening and release preparation

- [x] DTO/service validation hardening
- [x] Role, ownership, payment, receipt, review, admin, and WebSocket authorization tests
- [x] Transaction-boundary audit
- [x] Optional, idempotent, non-destructive demo seed paths
- [x] Final QA fixes, UI contracts, and documentation reconciliation through TASK-032
- [ ] Final human browser/demo review (outside implementation; required before release tag)

## Deliberate exclusions

Categories, service-package CRUD, booking-history tables, notifications, vouchers, AI, JWT, and production payments are not in the final implemented scope. No TASK-034 is planned.
