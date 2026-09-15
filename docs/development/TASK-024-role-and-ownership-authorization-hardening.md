# TASK-024: Role and Ownership Authorization Hardening

## Purpose

Complete the Week 7 `Fix authorization` and Phase 9 `Authorization tests` items by making protected MVC role checks consistent and adding service-layer defense in depth for identity-derived operations.

## Scope

- Centralize type-safe session user/role parsing for CUSTOMER, PHOTOGRAPHER, and ADMIN routes.
- Require `CUSTOMER` for booking creation/management, deposits, and reviews.
- Require `PHOTOGRAPHER` for portfolio and photographer booking management.
- Preserve booking, portfolio, deposit, review, and chat ownership checks based on the server-side session user ID and domain relationships.
- Prevent ADMIN/PHOTOGRAPHER accounts from creating customer bookings through direct service calls.
- Prevent an ADMIN or existing non-customer account from being demoted through photographer onboarding.
- Reject inactive or role-mismatched booking-chat participants.
- Rotate the HTTP session identifier after successful login.
- Replace wildcard WebSocket origins with a configurable trusted-origin allowlist.

## Files Affected

### Created

- `src/main/java/com/photoconnect/util/SessionSecurityUtils.java`
- `src/test/java/com/photoconnect/util/SessionSecurityUtilsTest.java`
- `docs/development/TASK-024-role-and-ownership-authorization-hardening.md`

### Modified

- `src/main/java/com/photoconnect/util/AdminSecurityUtils.java`
- `src/main/java/com/photoconnect/controller/BookingController.java`
- `src/main/java/com/photoconnect/controller/DepositController.java`
- `src/main/java/com/photoconnect/controller/ReviewController.java`
- `src/main/java/com/photoconnect/controller/PhotographerPortfolioController.java`
- `src/main/java/com/photoconnect/controller/PhotographerBookingController.java`
- `src/main/java/com/photoconnect/controller/LoginController.java`
- `src/main/java/com/photoconnect/service/BookingServiceImpl.java`
- `src/main/java/com/photoconnect/service/ChatServiceImpl.java`
- `src/main/java/com/photoconnect/service/PhotographerProfileServiceImpl.java`
- `src/main/java/com/photoconnect/config/WebSocketConfig.java`
- `src/main/resources/application.properties`
- related controller/service tests

## Acceptance Criteria

- Unauthenticated protected MVC requests redirect to `/login`.
- Authenticated users with the wrong role are redirected away before a protected service is called.
- Customer and photographer identities always come from the server-side session; submitted ownership IDs are not trusted.
- Service-layer calls cannot bypass customer role or onboarding role constraints.
- Only active CUSTOMER/PHOTOGRAPHER booking participants can use chat.
- WebSocket handshake origins default to `http://localhost:8080`; additional origins require `WEBSOCKET_ALLOWED_ORIGINS`.
- Existing admin authorization reuses the same role parser.

## Automated Tests

- `SessionSecurityUtilsTest`: missing, malformed, wrong-role, enum/string-compatible, and matching-role sessions.
- `BookingControllerTest`: photographer blocked from CUSTOMER booking form before service access.
- `PhotographerPortfolioControllerTest`: customer blocked from photographer portfolio before service access.
- `BookingServiceTest`: direct non-customer booking call rejected; self-booking regression retained.
- `PhotographerProfileServiceTest`: ADMIN onboarding/demotion rejected.
- `ChatServiceTest`: inactive participant denied before message persistence.
- Existing login, deposit, review, and role-controller tests retained.

Focused verification: 89 tests, 0 failures, 0 errors, 0 skipped.

## Manual Verification Steps

**Human Verification: PENDING**

1. Account: `PHOTOGRAPHER`; URL: `/photographers/{approvedProfileId}/book`; expect redirect to `/` and no booking form.
2. Account: `CUSTOMER`; URL: `/photographer/portfolio`; expect redirect to `/` and no portfolio data.
3. Account: `CUSTOMER`; URL: `/bookings/{ownBookingId}`; expect normal access. Change the ID to another customer's booking; expect denial/redirect without data disclosure.
4. Account: the photographer attached to the same booking; URL: `/photographer/bookings/{bookingId}` and `/bookings/{bookingId}/chat`; expect access. Use a different photographer account; expect denial.
5. Configuration: leave `WEBSOCKET_ALLOWED_ORIGINS` unset and verify chat works from `http://localhost:8080`; a cross-origin WebSocket handshake must not be accepted.

## Documentation

- `application.properties` documents the WebSocket origin environment variable.
- Full-batch verification: 360 tests, 0 failures, 0 errors, 23 skipped environment-gated integration tests; clean WAR packaging succeeded.
- `docs/ROADMAP.md`, `docs/PLAN_BE.md`, and `docs/final/project-status.md` were updated after verification.

## Status

Implementation, focused verification, full regression, and clean packaging complete. Human Verification: PENDING.
