# TASK-010: Public Photographer Marketplace

## Task ID
TASK-010

## Goal
Implement the first public photographer marketplace. Guests and logged-in users can:
1. Open `/photographers` — see only APPROVED photographer profiles
2. Open `/photographers/{id}` — see a photographer detail page
3. Never see PENDING, REJECTED, or SUSPENDED profiles publicly

## Context
TASK-009 built admin approval/rejection flow. Approved profiles exist in the DB.
`PhotographerProfile` has a lazy User relationship; `open-in-view=false`.
Existing repository already has JOIN FETCH queries from TASK-009 admin work.

## Business Rules
- Public list must include ONLY `verificationStatus = APPROVED`.
- Public list must exclude PENDING, REJECTED, SUSPENDED.
- Direct `/photographers/{id}` access must enforce the same approved-only rule.
- Filtering is enforced at service/repository layer — NOT just in JSP.
- No login required for `/photographers` or `/photographers/{id}`.
- Public DTO must NOT expose password, email, or admin-only fields.
- `open-in-view=false` must remain. No EAGER relationship changes.

## Expected Files
- `src/main/java/com/photoconnect/dto/PhotographerPublicDto.java` [NEW]
- `src/main/java/com/photoconnect/service/PublicPhotographerService.java` [NEW]
- `src/main/java/com/photoconnect/service/PublicPhotographerServiceImpl.java` [NEW]
- `src/main/java/com/photoconnect/controller/PhotographerController.java` [NEW]
- `src/main/webapp/WEB-INF/views/photographers.jsp` [NEW]
- `src/main/webapp/WEB-INF/views/photographer-detail.jsp` [NEW]
- `src/main/java/com/photoconnect/repository/PhotographerProfileRepository.java` [MODIFY — add approved-by-id query]
- `src/main/webapp/WEB-INF/views/index.jsp` [MODIFY — fix navbar + CTA links]
- `src/test/java/com/photoconnect/service/PublicPhotographerServiceTest.java` [NEW]
- `src/test/java/com/photoconnect/controller/PhotographerControllerTest.java` [NEW]
- `src/test/java/com/photoconnect/repository/PhotographerProfileRepositoryIntegrationTests.java` [MODIFY — add approved-by-id integration test]
- `docs/development/TASK-010-public-photographer-marketplace.md` [NEW]

## Checklist
- [x] Inspect project instructions
- [x] Inspect photographer approval model
- [x] Add repository query for APPROVED profiles (by id + status)
- [x] Create public photographer service
- [x] Create safe photographer view DTO
- [x] Implement photographer listing route
- [x] Implement photographer detail route
- [x] Prevent non-approved profiles from public access
- [x] Create photographer listing JSP
- [x] Create photographer detail JSP
- [x] Update navbar links
- [x] Update homepage CTA
- [x] Write service tests
- [x] Write controller tests
- [x] Verify APPROVED-only visibility
- [x] Verify non-approved detail access protection
- [x] Run all regression tests
- [x] Build WAR successfully
- [x] Start application successfully
- [ ] Await human browser verification
- [x] Write development documentation
- [x] Update final project status

## TDD Plan

### PublicPhotographerServiceTest
1. `listApproved_shouldReturnOnlyApprovedProfiles` — given approved + pending + rejected, only approved appears
2. `listApproved_emptyWhenNoneApproved` — no approved → empty list
3. `getById_approvedProfile_shouldReturnDto`
4. `getById_pendingProfile_shouldThrowIllegalArgumentException`
5. `getById_rejectedProfile_shouldThrowIllegalArgumentException`
6. `getById_suspendedProfile_shouldThrowIllegalArgumentException`
7. `publicDto_shouldNotExposePasswordOrEmail`

### PhotographerControllerTest
1. `GET /photographers` → 200, view="photographers", model has "photographers"
2. `GET /photographers` guest (no session) → 200 (public, no redirect)
3. `GET /photographers/{id}` approved → 200, view="photographer-detail"
4. `GET /photographers/{id}` not-approved → redirect to /photographers

### PhotographerProfileRepositoryIntegrationTests (addition)
1. `findByIdAndVerificationStatusWithUser_approvedProfile_shouldReturnResult`
2. `findByIdAndVerificationStatusWithUser_pendingProfile_shouldReturnEmpty`

## Verification Commands
- `mvn test`
- `mvn clean package`
- `mvn spring-boot:run`

## Problems Encountered
1. **EL method call limitations**: Standard JSP EL does not support `.substring()`, `.toUpperCase()`, or `.size()`. Fixed by adding `fn` taglib and using `fn:substring`, `fn:toUpperCase`, `fn:length`.
2. **LocalDateTime vs fmt:formatDate**: JSTL `fmt:formatDate` requires `java.util.Date`, not `LocalDateTime`. Fixed by using `${photographer.createdAt.year}` bean property EL (consistent with existing admin JSPs).

## Result
Automated tests: PASS (77 tests, 0 failures)
APPROVED-only public list: PASS
Public detail: PASS
Non-approved protection: PASS (PENDING/REJECTED/SUSPENDED return empty from repository query)
Guest access: PASS (no session required)
Sensitive-data exposure check: PASS (DTO has no getPassword/getEmail)
Admin/onboarding regression: PASS
Maven build: PASS (WAR built successfully)
Application startup: PASS (port 8080, Started in ~3.9s)
Human marketplace verification: PENDING

## Status
COMPLETE (Automated implementation finished, waiting for human browser verification)

