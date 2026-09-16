# Project Status: PhotoConnect

## Current Architecture
- Client-Server Architecture
- Web Application (Monolith)

## Currently Installed / Verified
- Java 21
- Spring Boot 3.3.0
- Maven
- WAR packaging
- Spring Web
- JSP/JSTL (Jakarta)
- Bootstrap (CDN)
- Spring Data JPA
- Microsoft SQL Server JDBC Driver
- SQL Server connectivity
- Cloudinary Java SDK
- Spring WebSocket + STOMP (SockJS)

## Planned Technologies
- Spring Security
- JWT
- payment gateway sandbox integration
- advanced UI animation
- AI functionality

## Completed Features
- **Project Foundation (TASK-001)**: Created minimal Spring Boot structure, validated Java/Maven environment, verified build and startup.
- **JSP Home Page (TASK-002)**: Integrated JSP/JSTL into Spring Boot, created the base `HomeController`, and established basic Bootstrap layout for `index.jsp`.
- **SQL Server Database Foundation (TASK-003)**: Integrated Microsoft SQL Server JDBC Driver and Spring Data JPA. Added connectivity configuration, ran integration tests via TDD, and proved connection to local SQL Server instance.
- **User Domain Model (TASK-004)**: Implemented `User` entity, `UserRole` and `UserStatus` enums, and `UserRepository`. Verified mapping and persistence with `users` table via `ddl-auto=update` and integration tests.
- **User Registration (TASK-005)**: Added registration page (`/register`), `RegisterRequest` DTO validation, `UserService` for business logic, duplicate email prevention, BCrypt password hashing, and default role (`CUSTOMER`) / status (`ACTIVE`). Evaluated with `RegisterControllerTest` and `UserServiceTest`.
- **Session Login (TASK-006)**: Added login page (`/login`), `AuthService` for authentication, session creation and management, logout functionality (`/logout`), BCrypt verification (`matches()`), logged-in navbar state, and TDD validations. Evaluated with `LoginControllerTest` and `AuthServiceTest`. Manual login/logout flow verified in browser.
- **Photographer Profile Domain Model (TASK-007)**: Implemented `PhotographerProfile` entity, `PhotographerVerificationStatus` enum, and `PhotographerProfileRepository`. Established `@OneToOne` unique mapping with `User`. Added `PhotographerProfileRepositoryIntegrationTests` to verify persistence and constraints.
- **Photographer Onboarding (TASK-008)**: Added `/become-photographer` onboarding flow with `PhotographerProfileRequest` DTO validation, `PhotographerProfileService` (transactional profile creation + role promotion), `PhotographerOnboardingController`, `photographer-onboarding.jsp` form, and `photographer-onboarding-status.jsp`. userId always sourced from session. Duplicate profile protection at service and DB levels. Session `userRole` updated to PHOTOGRAPHER on success without re-login.
- **Admin Photographer Approval (TASK-009)**: Added `/admin/photographers` list and detail views, approve (`PENDING → APPROVED`) and reject (`PENDING → REJECTED`) POST actions, `AdminPhotographerService` with `InvalidStatusTransitionException` for invalid transitions, session-based ADMIN role authorization. `UserServiceTest` cleanup fixed to delete child profiles before users (FK constraint).
- **Public Photographer Marketplace (TASK-010)**: Added `/photographers` listing and `/photographers/{id}` detail public routes (no login required). `PhotographerPublicDto` exposes only safe fields (no password, no email). `PublicPhotographerService` enforces APPROVED-only filtering at the repository/JPQL layer — not just in JSP. Navbar Photographers link and homepage "Explore Photographers" CTA now route to real pages. `fn` taglib used for JSTL-compatible EL expressions (substring, length). `findByIdAndVerificationStatusWithUser` query prevents status-bypass via direct URL guessing. `open-in-view=false` maintained; JOIN FETCH ensures User proxy is initialized before DTO mapping.

- **Photographer Portfolio with Cloudinary (TASK-011)**: Added approved-photographer image upload, owner-only deletion, SQL Server metadata persistence, private portfolio management, and public galleries on approved photographer detail pages. Image type/size validation and partial-failure cleanup are enforced in the service layer. Database and Cloudinary credentials are supplied only through environment variables. Automated Cloudinary behavior is verified with mocks; real Cloudinary browser verification remains pending.
- **Premium Photographer Marketplace + Search & Filter (TASK-012)**: Upgraded `/photographers` with dynamic search by keyword (name, bio, city), filter by city, minimum/maximum starting price, and minimum experience. Enforced strict server-side `APPROVED`-only visibility at the JPQL query layer. Implemented cover image resolution using Cloudinary portfolio assets with an elegant dark editorial fallback. Completely redesigned marketplace UI with a dark editorial aesthetic, custom shared design system (`photoconnect.css`), responsive filter panel, interactive cards with hover lift, and refined empty states.
- **Premium Photographer Booking Flow (TASK-013)**: Implemented customer-to-photographer booking creation flow. Allows an authenticated `CUSTOMER` to request a photoshoot session with an `APPROVED` photographer via `GET /photographers/{id}/book` and `POST /photographers/{id}/book`. Enforces price snapshotting (`agreedPrice` locked in from photographer's `priceFrom`), prevents self-booking (`customer.id != photographer.user.id`), blocks non-approved/inactive accounts, strictly isolates customer identity to server-side session `userId`, sets initial status `PENDING`, and provides a dark editorial confirmation page (`/bookings/{id}/success`) with ownership verification.

- **Booking Management & Status Workflow (TASK-014)**: Completed full customer and photographer booking management panels. Photographers can view and manage inbound booking requests, accepting, rejecting, or completing them. Customers can view their bookings and cancel pending/accepted bookings. Added a centralized state machine protecting transitions in the Service layer (e.g. `PENDING` -> `ACCEPTED`) and fixed N+1 issues using `JOIN FETCH` repository queries. Manual runtime verification confirmed customer creation and photographer `PENDING -> ACCEPTED` transition (PASS); `ACCEPTED -> COMPLETED` verification is pending.
- **Deposit and Payment Foundation (TASK-015)**: Introduced `Deposit` entity (`@OneToOne` mapped to `Booking`) and `DepositStatus` to secure `ACCEPTED` bookings. Added a simulated development payment flow that automatically calculates a 30% deposit requirement, protects route authorization, and idempotently updates `Deposit` status to `PAID` with a fake reference. Controlled by `photoconnect.payment.simulation-enabled` flag. **Runtime Verification**: Confirmed PASS (30% deposit calculation, dev payment simulation, DEV reference generation).
- **UI Unification Pass (VISUAL-PASS-001)**: Replaced default Bootstrap components with the custom PhotoConnect editorial design system across all remaining pages including booking flows, deposit pages, photographer dashboards (portfolio, status, bookings), customer dashboards, and the admin panel. Consolidated shared CSS, improved responsive layout, unified `navbar.jsp` usage, removed legacy SaaS-style styling, and standardized currency to VND to enforce a premium photography-first aesthetic.
- **Photographer Availability and Scheduling (TASK-017)**: Implemented photographer availability management. Photographers can block specific dates in their dashboard. Validation added to `BookingService` to prevent scheduling on blocked dates. Resolved runtime ClassCastException where `userRole` session String was cast to `UserRole`. Completed language consistency pass standardizing all dates, numbers, and UI to English (`en_US`, `MMM d, yyyy`). Full test suite included (192 tests); schedule page rendering and session compatibility confirmed PASS; blocked-date rejection flow remains PENDING.
- **Real-Time Booking Chat (TASK-018)**: Implemented real-time messaging between booking participants using Spring WebSocket + STOMP (`/ws`, `/topic/booking/{id}/chat`, `/app/chat.send`) with persistent SQL Server storage (`messages` table), eager JOIN FETCH retrieval, session-derived authorization, automatic recipient resolution, unread tracking, and an automatic HTTP REST fallback (`/api/bookings/{id}/messages`). UI integrated into booking details and standalone editorial chat page (`chat.jsp`). 27 new tests added (219 total passing tests, 0 failures, 0 errors).
- **Booking Reviews & Photographer Ratings (TASK-019)**: Implemented customer review and rating system for completed bookings. Features `Review` entity mapped to `reviews` table with a unique constraint on `booking_id`, rating constraints (1–5), optional trimmed comments (<= 1000 chars), and server-side participant identity resolution from the `Booking` entity. Photographer profile stats (`averageRating`, `reviewCount`) are recalculated from persisted `Review` records. Integrated into booking details (`booking-detail.jsp`), dedicated submission form (`review-form.jsp`), and public photographer profile with client review lists and rating metrics (`photographer-detail.jsp`). 30 new tests added (249 total tests: 227 passing, 0 failures, 0 errors, 22 skipped).
- **Admin Dashboard & Management (TASK-020)**: Implemented comprehensive administrative management suite. Features platform KPI dashboard (`/admin/dashboard`) with aggregate counts and explicitly disclaimed deposit metrics (TASK-015 development simulation), user search and status management (`/admin/users`) with admin self-protection guards, expanded photographer management with verification filtering and rating statistics (`/admin/photographers`), read-only booking monitoring (`/admin/bookings`), and read-only review monitoring (`/admin/reviews`). Centralized session-based role authorization enforced via `AdminSecurityUtils`. 39 new tests added (288 total tests: 266 passing, 0 failures, 0 errors, 22 skipped).
- **Admin Review Moderation (TASK-021)**: Implemented review moderation lifecycle and dynamic rating recalculation. Features `ReviewStatus` enum (`VISIBLE`, `HIDDEN`), safe SQL Server migration `V008__add_review_status.sql`, admin moderation endpoints (`POST /admin/reviews/{id}/hide`, `POST /admin/reviews/{id}/unhide`) with feedback alerts and active filter preservation, immediate transactional photographer rating recalculation (`averageRating` and `reviewCount`) from only active `VISIBLE` reviews, public profile visibility isolation (`/photographers/{id}`), and customer booking transparency indicator (`Hidden by moderation`). 32 new tests added (320 total tests: 298 passing, 0 failures, 0 errors, 22 skipped).
- **Global Exception Handling & Error Code Standardization (TASK-022)**: Implemented centralized `@ControllerAdvice` (`GlobalExceptionHandler`) with intelligent dual-mode content negotiation, standard `ApiResponse<T>` JSON envelope conforming to `docs/API_CONTRACT.md`, full implementation of `docs/ERROR_CODES.md` taxonomy (`ErrorCode` enum), container `/error` interception with `AppErrorController`, custom dark editorial `error.jsp`, domain exception normalization, and `ChatApiController` refactoring. 16 new tests added (336 total tests: 314 passing, 0 failures, 0 errors, 22 skipped).
- **Server-Side Validation Hardening (TASK-023)**: Closed account, profile, marketplace search, booking, scheduling, and portfolio input-boundary gaps. Added BCrypt-length, phone-format, decimal precision/scale, text-length, identifier, same-day past-time, and null-safety checks at Bean Validation and service boundaries. Oversized portfolio captions are rejected before Cloudinary work.
- **Role and Ownership Authorization Hardening (TASK-024)**: Centralized type-safe session role enforcement, restricted customer and photographer MVC routes before service access, added service-layer role and active-participant defenses, prevented onboarding role demotion, rotated the session ID after login, and replaced wildcard WebSocket origins with a configurable allowlist.
- **Approved Photographer Search Pagination (TASK-025)**: Added validated zero-based pagination to `/photographers`, database-backed `Pageable` approved-only search/count queries, a 12-card MVC page size with a service cap of 24, total/page metadata, and accessible filter-preserving Previous/Next navigation.
- **Responsive UI Stabilization (TASK-026)**: Rebalanced the photographer profile and booking panel, constrained native date/time controls, added shared overflow/grid/form safeguards, improved mobile stacking and wide-table containment, repaired malformed booking-card markup, and made JSP navigation/form routes context-path safe. Automated layout contracts pass; human visual verification remains pending.
- **Transaction and Service Hardening Audit (TASK-027)**: Verified semantic transaction boundaries for booking, deposit, review/rating moderation, availability, and portfolio database operations. Added a reflection-based regression contract and retained explicit Cloudinary compensation for the non-transactional remote resource boundary.
- **Development Demo Data (TASK-028)**: Added both an idempotent SQL Server seed script (`docs/development/seed/V009__demo_seed_data.sql`) and a double-opt-in `demo-seed` Spring Boot profile (`DemoDataSeeder.java`). Safely creates 1 admin, 2 customers, and 14 APPROVED photographers across 3 cities and varying prices (enabling 2-page pagination verification), 1 PENDING photographer, bookings, deposits, reviews, and chat history without destructive operations.
- **Professional Demo Payment & Receipt Experience (TASK-029)**: Replaced the TASK-015 developer-facing action with an explicitly disclosed, local-only PhotoConnect demo checkout. Added Demo QR and deterministic demo-card methods, `PENDING -> PROCESSING -> PAID/FAILED` plus cancellation, server-generated `PC-yyyyMMdd-XXXXXXXX` references, pessimistic-lock/idempotency protection, owner-only results and printable receipts, responsive editorial payment UI, and nullable method/failure metadata via V010. No gateway, banking API, merchant account, card persistence, or real transfer exists. **Human Verification: PARTIAL (Core Flow PASS; Edge Cases Automated/Pending)**.

## Database Status
- SQL Server database `PhotoConnect` connectivity established.
- `users`, `photographer_profiles`, `portfolio_images`, `bookings`, `deposits`, `photographer_unavailable_dates`, `messages`, and `reviews` tables mapped with foreign keys.
- **Verification**: Database schemas accurately reflect JPA entity models. Migration `docs/development/migrations/V008__add_review_status.sql` adds review status; seed script `docs/development/seed/V009__demo_seed_data.sql` provides opt-in demo data; forward-only migration `docs/development/migrations/V010__professional_demo_payment.sql` adds nullable `payment_method` and `failure_reason` deposit metadata without changing historical rows.

## Test Status
- 396 tests run with 373 passing, 0 failures, 0 errors, and 23 skipped (environment-gated integration tests requiring a live SQL Server).
- `mvn clean package` succeeds and produces `target/photoconnect.war`.
- TDD approach strictly followed.

## How to Run the Project
- Run `mvn spring-boot:run` from the root directory to start the application on port 8080.
- Run `mvn package` to build the WAR artifact (`target/photoconnect.war`).
- Set `DB_USERNAME` and `DB_PASSWORD` before database-backed commands. Set the three Cloudinary environment variables documented in `docs/SETUP.md` before real uploads.

## Known Issues
- Real Cloudinary upload/delete browser verification is pending.
- Real payment gateway is intentionally not integrated; TASK-029 is an explicitly disclosed local demo payment environment.
- Human verification for TASK-029 core flow (photographer acceptance, ACCEPTED 30% deposit requirement, checkout render, demo QR payment option, calculation breakdown, PC-* reference, demo disclosures, legacy deposit compatibility & receipt, UI button consistency) is PASS. Edge cases (failed card, cancelled payment, duplicate POST, cross-customer IDOR, photographer/admin denial, DevTools amount tampering, full mobile responsive matrix) remain verified via automated tests.
- Human visual verification for photographer availability booking rejection and unblocking (TASK-017) is pending.
- Human visual verification for multi-user real-time chat (TASK-018) is pending.
- Human visual verification for customer review submission and rating updates (TASK-019) is pending.
- Human verification for validation edge cases (TASK-023) is partial: alphabetic phone rejection was manually verified (PASS); the remaining listed edge cases are pending.
- Human verification for cross-role, cross-owner, and WebSocket-origin authorization behavior (TASK-024) is pending.
- Human verification for multi-page marketplace navigation (TASK-025) is partial: primary marketplace pagination flow manually verified (PASS); complex filter edge cases pending.
- Human visual verification for the TASK-026 desktop/mobile responsive composition is pending.

## Current Task Status
- TASK-014: Booking Management & Status Workflow — Runtime: PENDING -> ACCEPTED (PASS); ACCEPTED -> COMPLETED (PENDING).
- TASK-015: Deposit and Payment Foundation — Human Verification: PASS.
- TASK-017: Photographer Availability and Scheduling — Human Verification: PENDING.
- TASK-018: Real-Time Booking Chat — Human Verification: PENDING.
- TASK-019: Booking Reviews & Photographer Ratings — Human Verification: PENDING.
- TASK-020: Admin Dashboard & Management — Human Verification: PASS.
- TASK-021: Admin Review Moderation — Human Verification: PASS.
- TASK-022: Global Exception Handling & Error Code Standardization — Human Verification: PASS.
- TASK-023: Server-Side Validation Hardening — Human Verification: PARTIAL (alphabetic phone rejection PASS).
- TASK-024: Role and Ownership Authorization Hardening — Human Verification: PENDING.
- TASK-025: Approved Photographer Search Pagination — Human Verification: PARTIAL (primary marketplace pagination PASS).
- TASK-026: Responsive UI Stabilization — Automated/code completion PASS; Human Verification: PENDING.
- TASK-027: Transaction and Service Hardening Audit — Automated/code completion PASS.
- TASK-028: Development Demo Data — Human Verification: PASS (local SQL Server seed execution and seeded photographer visibility verified in live application).
- TASK-029: Professional Demo Payment & Receipt Experience — Human Verification: PARTIAL (Core Flow PASS; Edge Cases Automated/Pending). UI action/button consistency pass completed.
