# TASK-005: User Registration

## Goal
Implement a complete user registration flow for PhotoConnect. A visitor must be able to open `GET /register`, see a JSP registration form, submit it using `POST /register`, and create a new CUSTOMER account in SQL Server.

## Context
This task establishes the primary onboarding flow for new users. It builds upon the User domain model created in TASK-004.

## Requirements
- DTO with Bean Validation (RegisterRequest)
- Password hashing (BCrypt)
- User service with business logic (duplicate email check, default roles)
- Registration controller (GET/POST with PRG pattern)
- JSP form with Bootstrap styling
- Service and controller tests
- Do NOT implement login, Spring Security filter chains, or JWT.

## Expected Files
- `src/main/java/com/photoconnect/dto/RegisterRequest.java`
- `src/main/java/com/photoconnect/service/UserService.java`
- `src/main/java/com/photoconnect/controller/RegisterController.java`
- `src/main/java/com/photoconnect/config/SecurityConfig.java` (for PasswordEncoder)
- `src/main/webapp/WEB-INF/views/register.jsp`
- `src/test/java/com/photoconnect/service/UserServiceTest.java`
- `src/test/java/com/photoconnect/controller/RegisterControllerTest.java`
- `process/tasks/TASK-005-user-registration.md`
- `docs/development/TASK-005-user-registration.md`

## Checklist
- [x] Inspect project instructions
- [x] Inspect existing User persistence model
- [x] Add validation dependency if required
- [x] Add password hashing support
- [x] Create RegisterRequest DTO
- [x] Add registration validation rules
- [x] Create UserService
- [x] Implement duplicate-email check
- [x] Implement password hashing
- [x] Implement default CUSTOMER role
- [x] Implement default ACTIVE status
- [x] Create RegisterController
- [x] Create register.jsp
- [x] Write service tests first
- [x] Write controller tests
- [x] Verify duplicate email behavior
- [x] Verify password is not stored in plaintext
- [x] Run all automated tests
- [x] Build WAR successfully
- [x] Start application successfully
- [x] Verify existing homepage still works
- [x] Await human registration verification
- [x] Write development documentation
- [x] Update final project status

## TDD Plan
### Service Tests
- Successful registration (persists User, role=CUSTOMER, status=ACTIVE, hashes password)
- Duplicate email failure (EmailAlreadyExistsException)
- Password mismatch failure

### Controller Tests
- `GET /register`: status 200, view=register
- `POST /register` (invalid): status 200, view=register, has errors
- `POST /register` (valid): status 3xx, redirect to `/register?success`

## Verification Commands
- `mvn test`
- `mvn clean package`
- `mvn spring-boot:run`

## Problems Encountered
- `mvn test` failed initially because the database password was not provided to the test environment. Resolved by passing `$env:DB_PASSWORD`.

## Result
Automated tests: PASS
Registration service: PASS
BCrypt verification: PASS
Duplicate email prevention: PASS
Controller tests: PASS
Maven build: PASS
Application startup: PASS
GET /register: PASS
Human registration form verification: PENDING

## Status
COMPLETE (Automated implementation finished, waiting for human verification)
