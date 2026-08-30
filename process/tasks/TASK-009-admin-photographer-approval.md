# TASK-009: Admin Photographer Approval

## Task ID
TASK-009

## Goal
Implement the first administrative workflow: an ADMIN user can list, view, approve, and reject photographer applications. Non-admin users must be denied access.

## Context
TASK-008 created the onboarding flow that produces PhotographerProfile records with verificationStatus=PENDING. This task builds the admin side that processes those pending applications.

## Business Rules
- Only ADMIN session users may access admin endpoints.
- UserId and role always come from server-side session, never from form/URL.
- Allowed status transitions: PENDING → APPROVED, PENDING → REJECTED.
- Rejecting an application does NOT change User.role back to CUSTOMER (role and verification status are separate concepts).
- Approve/reject actions must use POST (not GET).
- A clean application-level error for already-processed applications.

## Expected Files
- `src/main/java/com/photoconnect/service/AdminPhotographerService.java`
- `src/main/java/com/photoconnect/service/AdminPhotographerServiceImpl.java`
- `src/main/java/com/photoconnect/exception/InvalidStatusTransitionException.java`
- `src/main/java/com/photoconnect/controller/AdminPhotographerController.java`
- `src/main/webapp/WEB-INF/views/admin-photographers.jsp`
- `src/main/webapp/WEB-INF/views/admin-photographer-detail.jsp`
- `src/test/java/com/photoconnect/service/AdminPhotographerServiceTest.java`
- `src/test/java/com/photoconnect/controller/AdminPhotographerControllerTest.java`
- `docs/development/TASK-009-admin-photographer-approval.md`

## Checklist
- [x] Inspect project instructions
- [x] Inspect current role/session model
- [x] Inspect photographer verification model
- [x] Add repository queries for pending applications
- [x] Create AdminPhotographerService
- [x] Protect admin operations by role
- [x] Implement application list
- [x] Implement application detail view
- [x] Implement approve action
- [x] Implement reject action
- [x] Prevent invalid status transitions where appropriate
- [x] Create admin JSP pages
- [x] Write service tests
- [x] Write controller tests
- [x] Verify non-admin access is denied
- [x] Verify approval persists
- [x] Verify rejection persists
- [x] Run regression tests
- [x] Build WAR successfully
- [x] Start application successfully
- [ ] Await human browser/database verification
- [x] Write development documentation
- [x] Update final project status

## TDD Plan
### AdminPhotographerServiceTest
- Pending list returns only PENDING profiles
- Approve: PENDING → APPROVED persisted
- Reject: PENDING → REJECTED persisted
- Approve non-PENDING profile: InvalidStatusTransitionException
- Reject non-PENDING profile: InvalidStatusTransitionException
- Get by invalid ID: clean error

### AdminPhotographerControllerTest (authorization)
- Unauthenticated GET /admin/photographers → redirect /login
- CUSTOMER GET → 403/redirect
- PHOTOGRAPHER GET → 403/redirect
- ADMIN GET → 200
- Non-admin POST approve → denied
- ADMIN POST approve → redirect

## Verification Commands
- `mvn test`
- `mvn clean package`
- `mvn spring-boot:run`

## Problems Encountered
- `UserServiceTest.cleanup()` triggered FK violation because some test users had photographer profiles. Fixed by deleting all profiles before users in the cleanup method.
- Port 8080 occupied by previous process; killed before restarting.

## Result
Automated tests: PASS
Pending application list: PASS
Admin authorization: PASS
Approve transition: PASS
Reject transition: PASS
Invalid transition protection: PASS
Onboarding regression: PASS
Login/Registration regression: PASS
Maven build: PASS
Application startup: PASS
Human admin verification: PENDING

## Status
COMPLETE (Automated implementation finished, waiting for human browser/database verification)
