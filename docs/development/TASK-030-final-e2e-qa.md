# TASK-030 — Final End-to-End QA & Bug Fixing

## Objective

Conduct a comprehensive stabilization audit and feature freeze across the entire PhotoConnect application following the completion of TASK-001 through TASK-029. Identify integration/regression bugs, fix verified flaws with minimal surgical diffs, expand regression tests, and ensure the application is hardened for final UI polish and demo preparation.

## Technology Stack Enforcement

The strict technology constraints remain fully respected:
- Java 21, Spring Boot 3.3.0, Spring MVC, JSP/JSTL, plain JavaScript, custom CSS (`photoconnect.css`, `payment.css`), Spring Data JPA / Hibernate, Microsoft SQL Server.
- No React, Vue, Angular, Python, SPA architecture, or Node build system were introduced.

---

## Baseline Test Results

Before introducing any code changes for TASK-030:
- **Total Tests**: 396
- **Passed**: 373
- **Failures**: 0
- **Errors**: 0
- **Skipped**: 23 (environment-gated SQL Server integration tests)
- **Status**: BUILD SUCCESS (8.055 s)

---

## Role & Route Authorization Matrix

| Persona | Allowed Routes / Capabilities | Prohibited Operations & Enforced Defenses |
|---|---|---|
| **GUEST** | Homepage (`/`), marketplace listing (`/photographers`), photographer public detail (`/photographers/{id}`), login (`/login`), registration (`/register`). | Cannot view booking/payment/chat/admin routes. Redirected to `/login`. |
| **CUSTOMER** | Customer dashboard (`/customer/bookings`), booking details (`/bookings/{id}`), booking creation (`/photographers/{id}/book`), cancellation, chat (`/bookings/{id}/chat`), deposit checkout (`/bookings/{id}/deposit/checkout`), deposit receipt (`/bookings/{id}/deposit/receipt`), review creation (`/bookings/{id}/review`). | Cannot access admin routes (`/admin/**`), photographer-only management routes (`/photographer/**`), or manipulate another customer's bookings/payments (enforced via session `userId` ownership checks). Cannot access `/become-photographer` if already a photographer. |
| **PHOTOGRAPHER** | Onboarding status (`/photographer/onboarding-status`), profile edit (`/photographer/profile`), portfolio management (`/photographer/portfolio`), availability blocking (`/photographer/availability`), incoming requests (`/photographer/bookings`), accept/reject/complete bookings, booking chat (`/bookings/{id}/chat`), read-only customer reviews. | Cannot access admin routes (`/admin/**`), cannot pay deposits or access customer deposit checkout, cannot modify another photographer's portfolio/availability/bookings. |
| **ADMIN** | Admin dashboard (`/admin/dashboard`), photographer management & verification (`/admin/photographers`), user search and activation (`/admin/users`), booking monitoring (`/admin/bookings`), review moderation (`/admin/reviews`, `/admin/reviews/{id}/hide`, `/admin/reviews/{id}/unhide`). | Centralized check in `AdminSecurityUtils`. Cannot onboard as a photographer (`/become-photographer` redirects to `/admin/dashboard`). Cannot accidentally invoke customer-owned payment or review flows. |

---

## Detailed Audit Findings & Areas Audited

### 1. Booking State Machine & Lifecycle (PASS)
- Concept: `PENDING -> ACCEPTED -> COMPLETED` or `PENDING -> REJECTED`, plus customer cancellation for `PENDING`/`ACCEPTED`.
- Validated that `BookingServiceImpl` strictly validates state transitions and rejects illegal transitions with domain exceptions.
- Agreed price is locked in at creation time from the photographer's `priceFrom`.
- Schedule conflicts and blocked dates are validated before booking creation.

### 2. Payment & Deposit System (PASS)
- Verified integration of TASK-015 + TASK-029:
  - Deposit is server-calculated at exactly 30% of `agreedPrice`.
  - Only `ACCEPTED` bookings owned by the authenticated customer can initiate checkout.
  - Payment reference (`PC-yyyyMMdd-XXXXXXXX`) generated server-side.
  - Pessimistic locking prevents double-payment on concurrent/repeated requests.
  - No card or CVV data is ever logged or persisted.
  - Clear and visible demo disclosure on checkout and receipt: *"Demo Payment Environment"*, *"No real money will be transferred."*

### 3. Review & Rating Recalculation (PASS)
- Verified that only the booking's customer can submit a review, and only when the booking status is `COMPLETED`.
- Unique database constraint on `booking_id` prevents duplicate reviews.
- Admin hide (`/admin/reviews/{id}/hide`) excludes review from public display and immediately recalculates photographer `averageRating` and `reviewCount` using only `VISIBLE` reviews.
- Admin unhide restores review and recalculates statistics accurately.

### 4. Real-Time Chat & WebSocket (PASS with Bug Fix)
- Audited Spring WebSocket + STOMP configuration (`/ws`, `/topic/booking/{id}/chat`, `/app/chat.send`) and REST fallback (`/api/bookings/{id}/messages`).
- Participant authorization enforced in both WebSocket destination intercepts and REST controller.
- *Bug fixed*: In `chat.jsp`, `contextPath` was scoped inside `connectWebSocket()`, which caused a `ReferenceError` when falling back to REST polling or submitting via REST fallback. Moved `contextPath` to top-level script scope.
- *CSS fix*: Updated send button class in `chat.jsp` from obsolete `pc-btn-primary` to design system `btn btn-primary`.

### 5. Photographer Marketplace & Pagination (PASS)
- Verified server-side `APPROVED`-only filter in repository JPQL queries.
- Pagination is zero-based with bound checks (capped at 24 max per page, default 12).
- Search query and filter parameters are preserved across pagination controls.
- Unapproved or pending photographers never leak to the public marketplace.

### 6. Availability & Scheduling (PASS)
- Photographers can block and unblock dates with ownership checks in `ScheduleServiceImpl`.
- Booking service validates both photographer unavailable dates and existing accepted bookings before allowing creation.
- Past-date and past-time requests on current day are rejected.

### 7. Error Handling & Validation (PASS)
- Centralized `GlobalExceptionHandler` intercepts exceptions and renders editorial `error.jsp` without exposing stack traces or leaking Spring Whitelabel.
- Bean Validation enforced on DTOs; service layer provides second-line defense.
- Standard `ErrorCode` taxonomy and `ApiResponse<T>` envelope for REST APIs.

---

## Bugs Identified, Root Causes & Fixes

### Bug 1: `chat.jsp` JavaScript ReferenceError in REST Fallback & Parser Scoping Errors
- **Root Cause**:
  1. `const contextPath = '${pageContext.request.contextPath}';` was declared within the local function scope of `connectWebSocket()`. The functions `startRestPolling()` and the `chatForm` event listener referenced `contextPath` from outer scope, throwing a JavaScript `ReferenceError` if WebSocket failed to connect or fell back to REST.
  2. Line 154 contained an inline style ternary attribute `style="${msg.senderId == currentUserId ? 'color: var(--accent-gold, #c9a96e);' : ''}"`. VS Code's HTML/CSS language server validated the contents of `style="..."` as CSS, encountering unquoted `${...}` and ternary syntax, yielding `} expected`, `at-rule or selector expected`, and `empty CSS ruleset warning`.
  3. Lines 187-188 declared `const bookingId = ${booking.id};` and `const currentUserId = ${currentUserId};` at the top level of the `<script>` tag. The unquoted `${...}` is invalid outside template literals in standard JS/TS parsing, triggering `';' expected`. Furthermore, declaring `currentUserId` with `const` in global script scope caused VS Code's TypeScript service to flag `Cannot redeclare block-scoped variable 'currentUserId'`.
- **Fix**:
  1. Added CSS rule `.message-mine .message-sender { color: var(--accent-gold, #c9a96e); }` in `<style>`, eliminating all inline style ternaries from `<span class="message-sender">`.
  2. Attached server values via standard HTML5 data-attributes on `<div id="messagesContainer" data-booking-id="${booking.id}" data-current-user-id="${currentUserId}" data-context-path="${pageContext.request.contextPath}">`.
  3. Encapsulated the client script in an IIFE `(function () { 'use strict'; ... })();` and parsed variables safely via `parseInt(messagesContainer.dataset.bookingId, 10)`, `parseInt(messagesContainer.dataset.currentUserId, 10)`, and `messagesContainer.dataset.contextPath || ''`. The `<script>` block is now 100% valid JavaScript with zero global scope collisions or parser errors.

### Bug 2: `chat.jsp` Obsolete Button Class
- **Root Cause**: The send button in `chat.jsp` used `class="pc-btn-primary"`, an obsolete class not defined in `photoconnect.css`.
- **Fix**: Changed to design system standard `class="btn btn-primary"`.

### Bug 3: `booking-detail.jsp` Potential JSP Parse Exception on Null `deposit.paidAt`
- **Root Cause**: For legacy deposits with status `PAID` but null `paidAt`, `<fmt:parseDate value="${fn:substring(deposit.paidAt, 0, 10)}" pattern="yyyy-MM-dd" ... />` would evaluate on an empty string and throw a `JspException`, crashing the booking detail view.
- **Fix**: Added `<c:choose><c:when test="${not empty deposit.paidAt}">...<fmt:formatDate...></c:when><c:otherwise><div style="font-size: 1.1rem;">Recorded</div></c:otherwise></c:choose>` in `src/main/webapp/WEB-INF/views/booking-detail.jsp`.

### Bug 4: `booking-success.jsp` Missing Direct Navigation to Booking Details
- **Root Cause**: After creating a booking, the user was presented only with "Explore More Artists" and "Back to Home", forcing them to navigate back through the customer bookings list to view the newly created booking details.
- **Fix**: Added `<a href="${pageContext.request.contextPath}/bookings/${booking.id}" class="btn btn-primary">View Booking Details</a>` to the action buttons in `src/main/webapp/WEB-INF/views/booking-success.jsp`.

### Bug 5: Role Inconsistency on Onboarding and Registration Routes
- **Root Cause**:
  1. An authenticated `ADMIN` visiting `/become-photographer` was served the onboarding form, and submitting threw an `IllegalStateException` in `PhotographerProfileServiceImpl` instead of cleanly redirecting to `/admin/dashboard`.
  2. An authenticated user visiting `/register` was served the registration form instead of redirecting to `/` (unlike `LoginController` which redirects logged-in users).
- **Fix**:
  1. Added `session.getAttribute("userRole")` check in `PhotographerOnboardingController`: if `ADMIN`, redirects to `/admin/dashboard`.
  2. Added `session.getAttribute("userId")` check in `RegisterController`: if already authenticated, redirects to `/`.

### Bug 6: Java Compiler & Null-Safety Warnings
- **Root Cause**:
  1. In `BookingController.showBookingForm`, an unused local variable `Long userId = com.photoconnect.util.SessionSecurityUtils.userId(session);` generated an IDE unused-variable warning.
  2. In `PhotographerScheduleController.getPhotographerProfileId`, querying the profile without explicitly verifying `userId != null` exposed a potential null-dereference warning in static analysis.
- **Fix**:
  1. Removed the unused `userId` assignment in `BookingController.showBookingForm`.
  2. Added an explicit `if (userId == null) return null;` guard in `PhotographerScheduleController.getPhotographerProfileId`.
  3. Audited `WebSocketConfig` and `GlobalExceptionHandler`: remaining warnings are standard Spring framework parameter nullability heuristics; handlers correctly enforce `@NonNull` annotations where appropriate and remain safe.

---

## Regression Tests Added

1. **`RegisterControllerTest`**:
   - `testShowRegisterForm_WhenAlreadyAuthenticated_RedirectsHome()`: Verifies that authenticated users visiting `GET /register` are redirected to `/`.
   - `testProcessRegistration_WhenAlreadyAuthenticated_RedirectsHome()`: Verifies that authenticated users submitting `POST /register` are redirected to `/` and `userService.registerUser(...)` is never invoked.
2. **`PhotographerOnboardingControllerTest`**:
   - `adminGet_shouldRedirectToAdminDashboard()`: Verifies that an `ADMIN` visiting `GET /become-photographer` is redirected to `/admin/dashboard`.
   - `adminPost_shouldRedirectToAdminDashboard()`: Verifies that an `ADMIN` submitting `POST /become-photographer` is redirected to `/admin/dashboard`.
   - `adminStatusGet_shouldRedirectToAdminDashboard()`: Verifies that an `ADMIN` visiting `GET /photographer/onboarding-status` is redirected to `/admin/dashboard`.
3. **`Task030QaIntegrationContractTest`** (New test suite in `com.photoconnect.ui`):
   - `chatJspHasCorrectScopingNoDuplicateDeclarationsAndButtonClass()`: Verifies HTML5 `data-*` attributes for `bookingId`, `currentUserId`, and `contextPath`, confirms script is encapsulated in an IIFE, verifies absence of raw unquoted JSP EL in JS scope and absence of inline style ternaries, and checks standard `btn btn-primary` class.
   - `bookingDetailGuardsDepositPaidAtDateParsing()`: Verifies null-safe `<c:when test="${not empty deposit.paidAt}">` and fallback display.
   - `bookingSuccessProvidesDirectLinkToBookingDetail()`: Verifies direct link to `/bookings/${booking.id}` with button styling.

---

## Final Verification Summary

- **`mvn test`**:
  - **Tests run**: 404
  - **Failures**: 0
  - **Errors**: 0
  - **Skipped**: 23 (environment-gated SQL Server integration tests)
  - **Result**: BUILD SUCCESS
- **`mvn clean package`**:
  - **Tests run**: 404 (0 failures, 0 errors, 23 skipped)
  - **Artifact generated**: `target/photoconnect.war`
  - **Result**: BUILD SUCCESS

---

## Remaining Known Limitations

1. **Environment-Gated Integration Tests**: 23 repository integration tests require a live Microsoft SQL Server instance and are intentionally skipped during automated CI/offline test runs.
2. **Demo Payment Scope**: The payment workflow is an explicitly disclosed demo simulation (TASK-029). No external payment gateway, merchant account, banking API, or persistent card data is involved.
3. **WebSocket Scalability**: The STOMP chat implementation uses Spring's built-in in-memory message broker, designed for single-node development and demo deployment. Multi-instance production scaling would require a shared external STOMP broker (e.g., RabbitMQ).
4. **Cloudinary Remote Boundary**: Portfolio upload relies on external Cloudinary credentials. Unit and service tests verify rollback and compensation logic, but live uploads require valid environment keys.
5. **Manual Browser & Cross-Device Scenarios**: Browser JavaScript execution (SockJS/STOMP fallback polling in browser runtime, cross-tab chat synchronisation, real viewport rendering) has been validated via static contract tests, while interactive manual verification remains pending for live demo preparation.

---

## Manual Verification Status

- **Status**: **PENDING / UNTESTED IN BROWSER** (Automated QA & Implementation: **PASS**).
- All 404 automated tests pass without errors or failures.
- No fabricated manual test passes are recorded. Browser-based verification remains explicitly tracked for human rehearsal:

### Manual Verification Checklist (For Demo Preparation)

- [ ] **Guest Marketplace Browse**: Visit `/photographers`, search by keyword, verify filter retention across pages.
- [ ] **Customer Booking Flow**: Log in as customer, select photographer, submit booking, verify redirect to booking success and click "View Booking Details".
- [ ] **Photographer Accept**: Log in as photographer, accept the pending booking request.
- [ ] **Customer Demo Checkout**: Return to booking detail as customer, click "Pay Deposit", complete Demo Card or Demo QR payment, review generated receipt.
- [ ] **Chat Communication**: Open chat as customer and photographer in separate tabs, send messages, verify real-time display and REST fallback polling.
- [ ] **Shoot Completion & Review**: Mark shoot completed as photographer; leave review as customer; verify updated photographer rating on public profile.
- [ ] **Admin Moderation**: Log in as admin, visit `/admin/reviews`, hide customer review, verify immediate rating recalculation and public profile isolation.

