# PhotoConnect V4 — Full Project Audit

**Document:** `docs/PHOTOCONNECT_V4_AUDIT.md`  
**Date:** October 2026  
**Auditor:** Senior Java Spring Boot Architect & Application Security Engineer  
**Scope:** Architecture, Entities, Services, Security, Lifecycles, Concurrency, and Data Integrity

---

## 1. Executive Summary

PhotoConnect is a photography marketplace built on **Spring Boot 3.3.0**, **Java 21/26**, **JSP/JSTL**, **Bootstrap 5.3.3**, **SiteMesh 3.3.0**, **Spring Data JPA/Hibernate 6.5.2**, **SQL Server**, and **WebSocket/STOMP**.

The core foundation (user authentication via session/JWT/Google Supabase, photographer onboarding and admin verification, portfolio image management via Cloudinary, marketplace search, demo deposit checkout, and 2-column GitHub-inspired user profile with FullCalendar v6) is implemented and passing 28+ regression and contract tests.

However, from an end-to-end business workflow and production marketplace perspective, several critical capabilities are partially implemented or missing:
1. **Service Packages (Phase 1):** Missing. Photographers only have a single global `price_from` on `photographer_profiles`. Customers book without selecting specific packages or deliverables (duration, edited photos, delivery timeframe).
2. **Calendar Conflict & Availability Engine (Phase 2):** Partially implemented. The calendar displays bookings and manual events, but booking conflict validation is primitive (only daily date-blocking via `PhotographerUnavailableDate`, no `[newStart, newEnd)` time overlap detection, no duration on bookings, and no concurrency lock).
3. **Booking Lifecycle State Machine (Phase 3):** Partially implemented. Current statuses are only `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `COMPLETED`. Missing intermediate production stages (`DEPOSIT_PENDING`, `CONFIRMED`, `SHOOTING`, `POST_PROCESSING`, `DELIVERED`), no transition history log (`booking_history`), and status updates are scattered across controllers without a centralized transition policy.
4. **Photography Delivery Gallery (Phase 4):** Not implemented. There is no mechanism for photographers to upload, organize, and deliver finished client photo galleries (`DeliveryAlbum` / `DeliveredPhoto`) with authenticated download access.
5. **Real-time Notifications (Phase 5):** Partially implemented. WebSocket chat works over `/topic/booking/{id}/chat`, but persistent notification records (`notifications` table), unread notification counters, and private notification channels (`/user/queue/notifications`) are missing.
6. **Cancellation & Rescheduling (Phase 6):** Defective/Partially implemented. Only customers can cancel `PENDING`/`ACCEPTED` bookings; photographers cannot cancel, reasons are unrecorded, and rescheduling requests are unsupported.
7. **Photographer Analytics Dashboard (Phase 7):** Partially implemented. Basic counts (pending, upcoming shoots, portfolio) exist, but revenue accounting (distinguishing Gross Booking Value, Confirmed Deposits, and Completed Revenue), monthly booking/revenue charts, and package popularity analytics are missing.
8. **Admin Operations (Phase 8):** Partially implemented. Verification and user/review moderation exist; booking monitoring, cancellation auditing, dispute review, and marketplace transaction analytics are absent.
9. **Search & Recommendation Engine (Phase 9):** Partially implemented. Keyword, city, price range, and experience filters exist; multi-category filtering, date availability search, and transparent multi-factor ranking algorithms are absent.

---

## 2. Feature Inventory Matrix

| Feature / Domain | State | Implementation Details & Identified Deficiencies |
|---|---|---|
| **User Authentication & Roles** | Implemented and verified | Session auth, JWT cookie filter (`JwtAuthenticationFilter`), Google OAuth link via Supabase (`ExternalAuthService`). Roles `CUSTOMER`, `PHOTOGRAPHER`, `ADMIN`. |
| **Photographer Onboarding & Admin Approval** | Implemented and verified | Registration, onboarding wizard, pending/approved status workflow (`PhotographerVerificationStatus`), admin verification. |
| **Portfolio Image Management** | Implemented and verified | Cloudinary integration (`CloudinaryStorageService`), cover image selection, category tags, ownership checks. |
| **Service Packages** | **Not implemented** | No `ServicePackage` entity, no package CRUD for photographers, no package selector on booking form, no price/deliverable snapshot. |
| **Calendar & Availability** | **Partially implemented** | FullCalendar v6 rendered, `CalendarEvent` table for personal/blocked/session events. **Defect:** No slot-level conflict prevention; duration is hardcoded to 2 hours; no database locking on booking concurrency. |
| **Booking Lifecycle** | **Partially implemented** | Basic `PENDING` -> `ACCEPTED` -> `COMPLETED`/`CANCELLED`/`REJECTED`. Missing `CONFIRMED`, `SHOOTING`, `POST_PROCESSING`, `DELIVERED`. No `booking_history` audit trail. |
| **Deposit / Demo Payment** | Implemented and verified | 30% deposit calculation, QR and demo card payment simulation, idempotency guards, transaction references (`PC-YYYYMMDD-XXXX`). **Defect:** Paying deposit does not advance booking status to `CONFIRMED`. |
| **Photo Delivery Gallery** | **Not implemented** | No `DeliveryAlbum` or `DeliveredPhoto` entities, no client delivery portal, no secure Cloudinary delivery. |
| **Realtime Chat** | Implemented and verified | STOMP over WebSocket (`/ws`), message persistence in `messages`, mark as read. **Security Note:** Chat broadcast to `/topic/booking/{id}/chat` relies on client subscription hygiene; lacks server-side channel authorization interceptor. |
| **Realtime Notifications** | **Not implemented** | No `Notification` entity, no notification bell UI, no transaction-commit push events. |
| **Cancellation & Rescheduling** | **Defective** | Only customer can cancel; no reason logged; photographer cannot cancel; deposit refund state unrecorded; no rescheduling mechanism. |
| **Photographer Dashboard** | **Partially implemented** | Studio dashboard displays pending requests and portfolio metrics. Lacks financial breakdown (gross vs. deposit vs. completed), charts, and package analytics. |
| **Admin Operations** | **Partially implemented** | Users, photographers, reviews moderation implemented. Booking dispute handling, cancellation audit, and marketplace financial analytics missing. |
| **Marketplace Search & Ranking** | **Partially implemented** | Basic SQL filter by keyword/city/price/experience. Lacks multi-factor ranking, sorting options, and availability-based filtering. |
| **UI Design System (Midnight Blue)** | Implemented and verified | V3 Midnight Blue palette (`#0F172A`, `#2563EB`, `#F8FAFC`, `#070B12`), dark mode tokens inverted and validated via automated contract tests. |

---

## 3. Deep-Dive Security & Concurrency Analysis

### 3.1 Booking Concurrency & Race Conditions
- **Current State:** `BookingServiceImpl.createBooking` checks `scheduleService.isDateAvailable` which queries `photographer_unavailable_dates`.
- **Vulnerability:** If two customers submit booking requests for the same photographer for overlapping time slots simultaneously, both pass the check and both bookings are created in `PENDING` state. Furthermore, when the photographer accepts, `acceptBooking` does not check for concurrent overlapping bookings.
- **Remediation:** Introduce duration-based slot collision checking (`newStart < existingEnd AND newEnd > existingStart`) and pessimistic locking or serializable isolation during acceptance to prevent overlapping confirmed bookings.

### 3.2 Booking Status & Transition Security
- **Current State:** Status transitions are checked with simple `if (booking.getStatus() == BookingStatus.PENDING)` inside controller/service methods.
- **Vulnerability:** No central state machine or audit log. If a booking is accepted, deposit paid, and then cancelled through an uncoordinated route, data becomes inconsistent. Deposit status does not keep booking status in sync.
- **Remediation:** Centralize all status transitions in a dedicated `BookingStateMachineService` that enforces strict transitions, logs history into `booking_history`, and coordinates deposit and calendar states.

### 3.3 Photo Delivery & Cloudinary Asset Privacy
- **Current State:** Portfolio images use standard Cloudinary delivery URLs.
- **Vulnerability:** Client delivery photos are private personal photography (weddings, portraits, family). Publicly accessible or predictable Cloudinary URLs risk data leakage.
- **Remediation:** Ensure delivery albums require booking participant authentication. Use private delivery transforms or authenticated server-mediated streaming with signed URLs for delivery photos.

### 3.4 WebSocket Isolation
- **Current State:** Stomp messages for chat broadcast to `/topic/booking/{bookingId}/chat`.
- **Vulnerability:** If an attacker subscribes to `/topic/booking/1/chat`, the broker will broadcast messages unless destination access is filtered.
- **Remediation:** Implement ChannelInterceptor or user-specific private destination routing (`/user/queue/notifications`) validated against session attributes.

---

## 4. Phase-by-Phase Roadmap & Plan

1. **Phase 1 — Service Packages:** Create `ServicePackage` entity, migrations, photographer package management UI/API, public package display, booking package selection, and price/deliverable snapshot.
2. **Phase 2 — Calendar & Availability Engine:** Slot-level collision detection (`[start, end)` overlap), working hours, configurable duration, concurrency locking.
3. **Phase 3 — Booking State Machine:** Comprehensive statuses (`REQUESTED`, `ACCEPTED`, `DEPOSIT_PENDING`, `CONFIRMED`, `SHOOTING`, `POST_PROCESSING`, `DELIVERED`, `COMPLETED`, `CANCELLED`, `REJECTED`), `booking_history` entity and audit log, centralized transition engine.
4. **Phase 4 — Photography Delivery Gallery:** `DeliveryAlbum` and `DeliveredPhoto` entities, upload flow with Cloudinary, secure client preview/download portal, confirmation & revision requests.
5. **Phase 5 — Realtime Notifications:** `Notification` entity, event listener triggering STOMP WebSocket push after transaction commit, notification center bell in navbar, read/unread management.
6. **Phase 6 — Cancellation & Rescheduling:** Bi-directional cancellation with reason and deposit disposition, rescheduling request and accept/reject workflow.
7. **Phase 7 — Photographer Dashboard:** Real database analytics for gross booking value, confirmed deposits, completed revenue, monthly booking chart, package popularity.
8. **Phase 8 — Admin Operations:** Booking oversight, cancellation monitoring, dispute handling, marketplace financial reporting.
9. **Phase 9 — Search & Recommendation:** Rule-based multi-factor ranking, sorting (highest rated, price asc/desc, popularity), availability date search.
10. **Phase 10 — UX Polish:** Polish workflows with Midnight Blue styling, clear status pills, empty states, and feedback modals.
11. **Phase 11 — Testing & Quality Assurance:** Comprehensive unit, integration, and security tests for each workflow.
12. **Phase 12 — Architecture & Documentation:** Finalize diagrams, ERD, API specs, and maintain `PHOTOCONNECT_V4_PROGRESS.md`.
