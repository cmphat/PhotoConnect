# TASK-008: Photographer Onboarding

## Task ID
TASK-008

## Goal
Allow a logged-in PhotoConnect user to apply to become a photographer by completing a professional profile form. On submission, a PhotographerProfile is created with verificationStatus=PENDING and the user's role is updated to PHOTOGRAPHER.

## Context
TASK-007 established the PhotographerProfile entity and PhotographerProfileRepository. This task builds the full onboarding flow on top of it: form, validation, service logic, session update, and JSP pages.

## Business Rules
- Only authenticated (logged-in) users may access the onboarding flow.
- The userId always comes from the server-side session, NEVER from a form field or URL parameter.
- A user may have at most one PhotographerProfile.
- Successful onboarding sets: user.role = PHOTOGRAPHER and profile.verificationStatus = PENDING.
- The PHOTOGRAPHER role and the PENDING verification status are separate concepts.
- Profile creation and role update must be atomic (both succeed or both roll back).
- Duplicate profile attempts must produce a clean application-level error.

## Expected Files
- `src/main/java/com/photoconnect/dto/PhotographerProfileRequest.java`
- `src/main/java/com/photoconnect/exception/PhotographerProfileAlreadyExistsException.java`
- `src/main/java/com/photoconnect/service/PhotographerProfileService.java`
- `src/main/java/com/photoconnect/service/PhotographerProfileServiceImpl.java`
- `src/main/java/com/photoconnect/controller/PhotographerOnboardingController.java`
- `src/main/webapp/WEB-INF/views/photographer-onboarding.jsp`
- `src/main/webapp/WEB-INF/views/photographer-onboarding-status.jsp`
- `src/test/java/com/photoconnect/service/PhotographerProfileServiceTest.java`
- `src/test/java/com/photoconnect/controller/PhotographerOnboardingControllerTest.java`
- `docs/development/TASK-008-photographer-onboarding.md`

## Checklist
- [x] Inspect project instructions
- [x] Inspect authentication/session implementation
- [x] Inspect PhotographerProfile model
- [x] Create PhotographerProfileRequest DTO
- [x] Add validation rules
- [x] Create PhotographerProfileService
- [x] Require authenticated user
- [x] Prevent duplicate photographer profile
- [x] Create profile for session user only
- [x] Change user role to PHOTOGRAPHER
- [x] Set verification status to PENDING
- [x] Ensure profile creation and role update are transactional
- [x] Create PhotographerOnboardingController
- [x] Create onboarding JSP
- [x] Create pending-verification JSP
- [x] Update navigation appropriately
- [x] Write service tests
- [x] Write controller tests
- [x] Verify duplicate-profile protection
- [x] Verify unauthenticated access protection
- [x] Run regression tests
- [x] Build WAR successfully
- [x] Start application successfully
- [ ] Await human browser/database verification
- [x] Write development documentation
- [x] Update final project status

## TDD Plan
### PhotographerProfileServiceTest
- Successful onboarding (ACTIVE user, no profile)
- Duplicate profile rejection
- Missing user rejection
- Inactive/Banned user rejection

### PhotographerOnboardingControllerTest
- Unauthenticated GET → redirect /login
- Authenticated GET → 200, onboarding view
- Invalid POST → onboarding view with errors
- Valid POST → redirect /photographer/onboarding-status, session role updated
- Already-has-profile GET → redirect /photographer/onboarding-status
- Authenticated onboarding status GET → 200

## Verification Commands
- `mvn test`
- `mvn clean package`
- `mvn spring-boot:run`

## Problems Encountered
- Port 8080 was already occupied by the TASK-007 application process. Resolved by killing the running process before restarting.

## Result
Automated tests: PASS
Authenticated onboarding: PASS
Photographer profile creation: PASS
Role update: PASS
PENDING verification assignment: PASS
Duplicate protection: PASS
Transaction behavior: PASS
Registration/Login regression: PASS
Maven build: PASS
Application startup: PASS
Human onboarding verification: PENDING

## Status
COMPLETE (Automated implementation finished, waiting for human browser/database verification)
