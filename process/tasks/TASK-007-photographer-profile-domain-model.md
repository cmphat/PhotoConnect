# TASK-007: Photographer Profile Domain Model

## Goal
Create the foundational Photographer Profile domain model for PhotoConnect. A photographer profile represents the professional information associated with a User account.

## Context
This task focuses strictly on the domain model and database persistence layer. It does not include creating user interfaces, authorization logic, or search functionality.

## Requirements
- `PhotographerVerificationStatus` enum with PENDING, APPROVED, REJECTED, SUSPENDED.
- `PhotographerProfile` entity mapped to `photographer_profiles`.
- One-to-one relationship with `User`, unique constraint on `user_id`.
- Avoid destructive cascades on the User relationship.
- `PhotographerProfileRepository` for basic persistence.
- Integration tests simulating the repository operations and constraints.
- No UI components or complex business services yet.

## Expected Files
- `src/main/java/com/photoconnect/entity/PhotographerVerificationStatus.java`
- `src/main/java/com/photoconnect/entity/PhotographerProfile.java`
- `src/main/java/com/photoconnect/repository/PhotographerProfileRepository.java`
- `src/test/java/com/photoconnect/repository/PhotographerProfileRepositoryIntegrationTests.java`
- `docs/development/TASK-007-photographer-profile-domain-model.md`

## Checklist
- [x] Inspect project instructions
- [x] Inspect existing User model
- [x] Design PhotographerProfile relationship
- [x] Create PhotographerVerificationStatus enum
- [x] Create PhotographerProfile entity
- [x] Create PhotographerProfileRepository
- [x] Enforce one profile per User
- [x] Write repository integration tests
- [x] Verify relationship persistence
- [x] Verify unique user/profile relationship
- [x] Run all regression tests
- [x] Build WAR successfully
- [x] Start application successfully
- [x] Verify existing login/register behavior
- [x] Verify photographer_profiles table exists
- [x] Write development documentation
- [x] Update final project status
- [ ] Await human SQL Server verification

## TDD Plan
### PhotographerProfileRepositoryIntegrationTests
- **Create photographer user**: Create user with `role = PHOTOGRAPHER`.
- **Create PhotographerProfile**: Persist profile linked to user, verifying all fields (PENDING status, priceFrom mapping, non-null ID).
- **Unique relationship**: Verify one user cannot own two photographer profiles.
- **Cleanup**: Clean test data appropriately (child before parent).

## Verification Commands
- `mvn test`
- `mvn clean package`
- `mvn spring-boot:run`

## Problems Encountered
- `mvn spring-boot:run` failed initially because port 8080 was occupied by the previous task's server process. We resolved this by identifying and killing the background process on port 8080 before starting the new application context.

## Result
Automated tests: PASS
PhotographerProfile persistence: PASS
User/Profile relationship: PASS
Unique user/profile constraint: PASS
Maven build: PASS
Application startup: PASS
Registration/Login regression: PASS
Human SQL Server verification: PENDING

## Status
COMPLETE (Automated implementation finished, waiting for human SQL Server verification)
