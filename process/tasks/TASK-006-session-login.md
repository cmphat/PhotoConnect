# TASK-006: Session Login

## Goal
Implement a complete login flow using standard server-side HTTP Session authentication.

## Context
A registered PhotoConnect user must be able to open `/login`, enter email and password, authenticate using the existing BCrypt password hash, receive a server-side session, return to the homepage as a logged-in user, and log out and destroy the session.

## Requirements
* Use HTTP Session, not JWT.
* Do not use Spring Security filter chain.
* Safely verify BCrypt password hashes.
* Prevent inactive or banned users from logging in.
* Return a generic "Email or password is incorrect" message on failed login.
* Store safe session attributes (userId, userEmail, userFullName, userRole).

## Checklist
- [x] Inspect project instructions
- [x] Inspect existing User and password hashing implementation
- [x] Create LoginRequest DTO
- [x] Create authentication service logic
- [x] Verify email normalization
- [x] Verify password using BCrypt
- [x] Reject unknown email cleanly
- [x] Reject wrong password cleanly
- [x] Reject banned/inactive users appropriately
- [x] Create LoginController
- [x] Create login.jsp
- [x] Create session on successful login
- [x] Add logout endpoint
- [x] Update navbar for logged-in/logged-out states
- [x] Write authentication service tests first
- [x] Write controller/session tests
- [x] Run all automated tests
- [x] Build WAR successfully
- [x] Start application successfully
- [x] Verify existing registration still works
- [x] Await human login verification
- [x] Write development documentation
- [x] Update final project status

## TDD Plan
### AuthService Tests
- valid login
- wrong password
- unknown email
- inactive account
- banned account

### Controller/Session Tests
- GET /login
- invalid POST
- wrong credentials POST
- successful POST
- logout POST

## Verification Commands
- `mvn test`
- `mvn clean package`
- `mvn spring-boot:run`

## Problems Encountered
- `mvn test` execution requires `$env:DB_PASSWORD` to be set.

## Result
Automated tests: PASS
Valid login: PASS
Invalid credentials handling: PASS
Inactive/Banned protection: PASS
Session creation: PASS
Logout: PASS
Registration regression: PASS
Maven build: PASS
Application startup: PASS
Human login verification: PASS

## Status
COMPLETE
