# TASK-023: Server-Side Validation Hardening

## Purpose

Complete the Week 7 / Phase 9 validation hardening item from `docs/ROADMAP.md` and `docs/PLAN_BE.md` by closing input-boundary gaps discovered during the repository audit.

## Scope

- Enforce BCrypt's 72-character password boundary on login and confirmation input.
- Reject malformed phone values at registration.
- Keep photographer prices within the database-supported precision and scale.
- Bound public search text and experience filters.
- Reject same-day bookings whose time has already passed, including direct service calls that bypass MVC validation.
- Enforce booking location/note lengths in the service layer.
- Validate schedule identifiers, dates, and 255-character reasons without null-pointer failures.
- Enforce the 500-character portfolio caption limit before uploading to Cloudinary.

## Files Affected

### Modified

- `src/main/java/com/photoconnect/dto/RegisterRequest.java`
- `src/main/java/com/photoconnect/dto/LoginRequest.java`
- `src/main/java/com/photoconnect/dto/PhotographerProfileRequest.java`
- `src/main/java/com/photoconnect/dto/PhotographerSearchRequest.java`
- `src/main/java/com/photoconnect/service/BookingServiceImpl.java`
- `src/main/java/com/photoconnect/service/ScheduleServiceImpl.java`
- `src/main/java/com/photoconnect/service/PortfolioServiceImpl.java`
- `src/test/java/com/photoconnect/dto/PhotographerSearchRequestTest.java`
- `src/test/java/com/photoconnect/service/BookingServiceTest.java`
- `src/test/java/com/photoconnect/service/ScheduleServiceTest.java`
- `src/test/java/com/photoconnect/service/PortfolioServiceTest.java`

### Created

- `src/test/java/com/photoconnect/dto/AccountInputValidationTest.java`
- `docs/development/TASK-023-server-side-validation-hardening.md`

## Acceptance Criteria

- Invalid account/profile inputs fail Bean Validation before persistence.
- A booking for today with a time earlier than the server time is rejected.
- Oversized booking, schedule, search, and portfolio text is rejected before database or Cloudinary work.
- Null schedule input produces a controlled validation result, not an internal exception.
- Existing valid inputs and flows remain unchanged.

## Automated Tests

- `AccountInputValidationTest`: malformed phone, oversized BCrypt input, unsupported photographer price scale.
- `PhotographerSearchRequestTest`: oversized keyword/city and unrealistic experience bounds.
- `BookingServiceTest`: same-day past time and oversized location rejection before repository access.
- `ScheduleServiceTest`: null date, oversized reason, and null availability input.
- `PortfolioServiceTest`: oversized caption rejected before Cloudinary upload.

Focused verification: 56 tests, 0 failures, 0 errors, 0 skipped.

## Manual Verification Steps

**Human Verification: PARTIAL**

1. **PASS (manually verified by the project owner):** Account: guest; URL: `/register`; an alphabetic phone value was rejected by the form validation.
2. Account: `CUSTOMER`; URL: `/photographers/{approvedProfileId}/book`; select today's date with an already-passed time and confirm the booking is rejected.
3. Account: `PHOTOGRAPHER`; URL: `/photographer/schedule`; enter a reason longer than 255 characters and confirm it is rejected without adding a blocked date.
4. Account: approved `PHOTOGRAPHER`; URL: `/photographer/portfolio`; upload an otherwise valid image with a caption longer than 500 characters and confirm no image is uploaded.

## Documentation

- This task document records scope, acceptance criteria, tests, and pending human verification.
- Full-batch verification: 360 tests, 0 failures, 0 errors, 23 skipped environment-gated integration tests; clean WAR packaging succeeded.
- `docs/ROADMAP.md`, `docs/PLAN_BE.md`, and `docs/final/project-status.md` were updated after verification.

## Status

Implementation, focused verification, full regression, and clean packaging complete. Human Verification: PARTIAL (alphabetic phone rejection PASS; remaining cases pending).
