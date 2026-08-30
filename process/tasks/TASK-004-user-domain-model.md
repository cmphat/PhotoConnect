# TASK-004: User Domain Model

## Goal
Create the first real PhotoConnect domain model: `User`. Provide JPA mapping, enums, and a Spring Data repository for persistence.

## Context
This task establishes the base user entity mapped to the `users` table in SQL Server. No business logic (login, JWT, registration UI) is being built yet. 

## Expected files
- src/main/java/com/photoconnect/entity/UserRole.java
- src/main/java/com/photoconnect/entity/UserStatus.java
- src/main/java/com/photoconnect/entity/User.java
- src/main/java/com/photoconnect/repository/UserRepository.java
- src/test/java/com/photoconnect/repository/UserRepositoryIntegrationTests.java
- docs/development/TASK-004-user-domain-model.md
- docs/final/project-status.md
- process/tasks/TASK-004-user-domain-model.md
- application.properties (modified)

## Checklist
- [x] Inspect project instructions
- [x] Inspect current database configuration
- [x] Design User fields
- [x] Create UserRole enum
- [x] Create UserStatus enum
- [x] Create User entity
- [x] Create UserRepository
- [x] Configure schema generation safely for this task
- [x] Write repository test first
- [x] Verify failing state when applicable
- [x] Make persistence test pass
- [x] Verify users table exists in SQL Server
- [x] Run all automated tests
- [x] Build WAR successfully
- [x] Start application successfully
- [x] Verify homepage still works
- [x] Write development documentation
- [x] Update final project status
- [x] Await human database verification

## TDD plan
Write a `UserRepositoryIntegrationTests` that verifies saving a `User`, retrieving by email, testing `existsByEmail`, and validating enum mapping. Test email should be unique.

## Verification commands
- `mvn test` (with DB env vars)
- `mvn clean package`
- `mvn spring-boot:run`

## Problems encountered
- Previous Tomcat instance on port 8080 was hanging; killed process manually.

## Result
Automated tests: PASS
User persistence: PASS
users table creation: PASS
Maven build: PASS
Application startup: PASS
Homepage regression: PASS
Human SQL Server table verification: PASS

## Status
COMPLETE
