# PhotoConnect V5 — Release Candidate Report & Academic Deliverable

**Date:** 2026-10-25 (Asia/Saigon)  
**Branch:** `v5-master-development`  
**Target Release:** PhotoConnect V5 Release Candidate (RC-1)  
**Verification Baseline:** 902 Tests Run (902 Passed, 0 Failures, 0 Errors, 0 Skipped)  
**Execution Environment:** Java 21 LTS, Spring Boot 3.3.0, Apache Maven 3.9.11, Microsoft SQL Server 2022  

---

## 1. Executive Summary

PhotoConnect V5 represents the culmination of an 8-sprint, 22-phase autonomous engineering lifecycle to transform PhotoConnect from a secured baseline into a release-candidate photography marketplace platform.

Key achievements in V5:
1. **End-to-End User Verification & Authentication**: Cryptographically secure 6-digit Email OTP with BCrypt hashing, local thread-safe email capture mode, sliding-window rate limiting, and temporary lockout against credential stuffing.
2. **Operations & Governance (MANAGER Role)**: Distinct operational governance tier (ADMIN, MANAGER, CUSTOMER, PHOTOGRAPHER) with dedicated Manager Dashboard (`/manager/dashboard`), dispute resolution with optimistic locking, photographer application review, user report investigation, and photographer suspension workflows.
3. **Photographer Suspension Lifecycle**: Fully exposed and audited suspension transitions (`APPROVED -> SUSPENDED` and `SUSPENDED -> APPROVED`) across Admin and Manager services, controllers, and UI views.
4. **Professional Marketplace & Discovery**: Side-by-side package comparison (up to 3 packages) with floating selection dock, 10-package active ceiling, and cold-start exploration boost (+0.15) for newly approved creators.
5. **Structured Creative Collaboration**: Structured **Shoot Brief** engine capturing concepts, Pinterest/Drive moodboards, backup locations, shot lists, and prop requirements.
6. **Studio CRM & Relationship Intelligence**: Private creator notes and client booking frequency tracking (`Khách quen` vs `Khách hàng mới`).
7. **Trust, Safety & Incident Reporting**: Incident violation reports queue with investigation workflows (`resolveReport`, `dismissReport`) and operational notice banners on pending disputes.
8. **Performance, Migration & Security Hardening**: Flyway V001–V027 migrations with error 1785 resolution (`ON DELETE NO ACTION`), HikariCP connection pool optimization, Hibernate JDBC batching, defense-in-depth security headers (`nosniff`, `SAMEORIGIN`, `strict-origin-when-cross-origin`, `Permissions-Policy`), and sliding-window login brute-force defense.
9. **Strict Architecture & Layout Preservation**: Exactly 45 content JSPs and 2 decorators strictly enforcing the Midnight Blue design tokens with zero unclassed buttons.

---

## 2. Core Architecture & Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Language & Runtime** | Java 21 LTS (Oracle OpenJDK) | Modern language features (records, pattern matching, virtual-thread friendly) |
| **Framework** | Spring Boot 3.3.0 | Spring MVC, Spring Data JPA, Spring Security, Spring WebSocket |
| **View Layer** | Jakarta JSP 3.1 / JSTL 3.0 | SiteMesh 3 decorator mesh, Bootstrap 5.3.3, Vanilla CSS design tokens |
| **Design System** | Midnight Blue | Primary `#2563EB`, Hover `#1D4ED8`, Navy `#0F172A`, Deep Navy `#172554`, Surface `#FFFFFF`, Canvas `#F8FAFC`, Border `#CBD5E1` |
| **Database** | Microsoft SQL Server 2022 | Flyway 10.10.0 versioned migrations (`V001` through `V027`) |
| **ORM & Concurrency** | Hibernate 6.5 / Spring Data JPA | `open-in-view=false`, explicit `JOIN FETCH` queries, optimistic versioning (`@Version`) on disputes, pessimistic locking on financial deposits |
| **Media Delivery** | Cloudinary Private CDN | Time-limited signed authenticated URLs; strict DTO redaction of raw storage identifiers |
| **Real-time Messaging** | Spring WebSocket & STOMP | Channel interceptor access control; booking room isolation |

---

## 3. Comprehensive Sprint & Phase Matrix (Phases 0 – 22)

| Phase | Sprint | Name | Status | Key Deliverables & Artifacts |
|---|---|---|---|---|
| **Phase 0** | Sprint 1 | Baseline & Stabilization | **TESTED** | Baseline verification report, test suite stabilization. |
| **Phase 1** | Sprint 2 | Identity, Email Verification & OTP | **TESTED** | `EmailVerificationToken`, 6-digit OTP, BCrypt salted hashing, 5m expiry, email capture mode, `V022`. |
| **Phase 2** | Sprint 2 | Professional Profiles & Social Channels | **TESTED** | TikTok & YouTube channels, external link sanitization (`http`/`https`, `noopener`), `V023`. |
| **Phase 3** | Sprint 2 | Onboarding & Re-application Workflow | **TESTED** | Admin rejection rationale, photographer re-apply revision loop (`REJECTED` -> `PENDING`), `V023`. |
| **Phase 4** | Sprint 3 | Marketplace & Side-by-Side Comparison | **TESTED** | Max 10 active packages, recommended tier highlight, floating comparison dock, comparison modal. |
| **Phase 5** | Sprint 3 | Discovery & Recommendations | **TESTED** | Cold-start exploration boost (+0.15) for new talent, quick budget chips, city suggestions datalist. |
| **Phase 6** | Sprint 4 | Calendar & Availability Engine | **TESTED** | Interactive availability slot fetcher in `booking-form.jsp`, interval collision checks. |
| **Phase 7** | Sprint 5 | Booking Detail & Shoot Brief | **TESTED** | Structured Shoot Brief fields (concept, moodboard, backup location, shot list, props, participants), `V024`. |
| **Phase 8** | Sprint 4 | Deposit & Payment Consistency | **TESTED** | 30% deposit calculation (`BigDecimal`), pessimistic locking, idempotent state transitions including `DEPOSIT_PENDING`. |
| **Phase 9** | Sprint 4 | Messaging & STOMP Chat Collaboration | **TESTED** | STOMP chat broker, booking participant authorization, batch unread tracking. |
| **Phase 10** | Sprint 4 | Notification Center & Delivery | **TESTED** | Unread notifications badge polling, mark-all-read and mark-single-read workflows. |
| **Phase 11** | Sprint 5 | Secure Photo Delivery & Revision Workflow | **TESTED** | Cloudinary private signed downloads, revision feedback loop, customer acceptance trigger. |
| **Phase 12** | Sprint 6 | Customer Workspace & Dashboard | **TESTED** | Status filtering (`UPCOMING`, `PENDING`, `COMPLETED`, `CANCELLED`), interactive filter pills, empty state. |
| **Phase 13** | Sprint 6 | Photographer Studio & CRM | **TESTED** | Creator-private notes (`photographer_notes`), client booking history counter (`Khách quen`), `V026`. |
| **Phase 14** | Sprint 5 | Reviews, Trust & Reporting | **TESTED** | `user_reports` table (`V025`), dispute categories, customer reporting modal, admin moderation queue. |
| **Phase 15** | Sprint 6 | Admin & Manager Operations | **TESTED** | MANAGER role, Manager operations dashboard, report moderation, booking dispute resolution, photographer suspension (`V027`). |
| **Phase 16** | Sprint 7 | Complete UI/UX Polish (Midnight Blue) | **TESTED** | Viewport responsive layouts (375px to 1440px), 45 content JSPs audited, natural Vietnamese copy. |
| **Phase 17** | Sprint 7 | Performance, Query Optimization & Logging | **TESTED** | HikariCP pool (15 max, 5 idle), Hibernate batching (25), structured logging levels. |
| **Phase 18** | Sprint 7 | Security Hardening & Penetration Defense | **TESTED** | `SecurityHeadersFilter` (`nosniff`, `SAMEORIGIN`), `LoginRateLimiter` sliding window brute-force protection. |
| **Phase 19** | Sprint 8 | Database Migration & Baseline Verification | **TESTED** | Full audit of Flyway migrations V001–V027, error 1785 fixed, `FlywayConfig` auto-repair, `FlywayMigrationContractTest`. |
| **Phase 20** | Sprint 8 | Automated Testing & End-to-End QA | **TESTED** | 902 tests executed on live isolated SQL Server 2022 database: 902 passed, 0 failures, 0 errors, 0 skipped. |
| **Phase 21** | Sprint 8 | Error Handling, Edge Cases & Resilience | **TESTED** | Dual-mode `GlobalExceptionHandler`, HTTP 400/403/404/500 handlers, branded `error.jsp`. |
| **Phase 22** | Sprint 8 | Academic Deliverables & Demo Environment | **TESTED** | Comprehensive release report, demo credentials walkthrough across 4 roles, clean git history. |

---

## 4. Demo Environment & Role Credentials

For academic review, demonstration, and staging evaluation, PhotoConnect provides pre-seeded accounts representing all four platform roles:

| Role | Email | Password | Primary Capabilities to Demonstrate |
|---|---|---|---|
| **Customer** | `customer@demo.photoconnect.local` | `PhotoConnectDemo!2026` | Explore photographers, compare packages, book with Shoot Brief, checkout deposit, private delivery review, file report. |
| **Photographer** | `photographer01@demo.photoconnect.local` | `PhotoConnectDemo!2026` | Manage packages (max 10), configure calendar slots, view shoot brief, write private CRM notes, upload deliverables. |
| **Operations Manager** | `manager@demo.photoconnect.local` | `PhotoConnectDemo!2026` | Manager operations dashboard (`/manager/dashboard`), review photographer applications, resolve disputes, investigate user reports, suspend photographers if justified. |
| **Administrator** | `admin@demo.photoconnect.local` | `PhotoConnectDemo!2026` | Review photographer applications (approve/reject/suspend), manage user roles (grant/revoke `MANAGER`), inspect tamper-resistant audit logs (`/admin/audit`), platform KPIs. |

### Operational Modes:
- **Email Delivery**: When `MAIL_USERNAME` is unconfigured, the application runs in `MAIL_CAPTURE_MODE=true`. All verification OTPs and notification emails are captured safely in `EmailCaptureStore` without leaking to external SMTP.
- **Deposit Checkout**: Running in `photoconnect.payment.simulation-enabled=true`. Demonstrates atomic deposit confirmation and receipt generation without transferring real funds.

---

## 5. Verification & Test Metrics

```
================================================================================
PHOTO CONNECT V5 — TEST SUITE EXECUTION SUMMARY (ISOLATED SQL SERVER 2022)
================================================================================
Total Tests Run:     902
Passed:              902
Skipped:             0
Failures:            0
Errors:              0
Build Status:        BUILD SUCCESS (Total time: 35.774 seconds)
================================================================================
```

### Critical Architectural Contracts Maintained:
1. **JSP Page Count Contract**: Exactly 45 content JSPs and 2 decorators in `src/main/webapp/WEB-INF/views` (enforced by `BootstrapIntegrationContractTest`, `SiteMeshIntegrationContractTest`, and `Sprint7QualityContractTest`).
2. **Button Styling Contract**: Zero unclassed `<button>` tags across all 45 JSPs.
3. **Monetary Arithmetic Contract**: All deposit amounts, platform fees, and package prices strictly use `BigDecimal` with half-up rounding.
4. **Security Filter Ordering**:
   - `SecurityHeadersFilter`: `Ordered.HIGHEST_PRECEDENCE + 10`
   - `JwtAuthenticationFilter`: `Ordered.HIGHEST_PRECEDENCE + 20`
   - `CsrfFilter`: `Ordered.HIGHEST_PRECEDENCE + 30`
5. **Database Migration Contract**: Flyway V001 through V027 apply cleanly on fresh installs without error 1785, with automatic schema checksum alignment via `FlywayConfig`.
