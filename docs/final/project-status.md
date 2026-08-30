# Project Status: PhotoConnect

## Current Architecture
- Client-Server Architecture
- Web Application (Monolith)

## Currently Installed / Verified
- Java 21
- Spring Boot 3.3.0
- Maven
- WAR packaging
- Spring Web
- JSP/JSTL (Jakarta)
- Bootstrap (CDN)
- Spring Data JPA
- Microsoft SQL Server JDBC Driver
- SQL Server connectivity
- Cloudinary Java SDK

## Planned Technologies
- Spring Security
- JWT
- WebSocket
- payment integration
- advanced UI animation
- AI functionality

## Completed Features
- **Project Foundation (TASK-001)**: Created minimal Spring Boot structure, validated Java/Maven environment, verified build and startup.
- **JSP Home Page (TASK-002)**: Integrated JSP/JSTL into Spring Boot, created the base `HomeController`, and established basic Bootstrap layout for `index.jsp`.
- **SQL Server Database Foundation (TASK-003)**: Integrated Microsoft SQL Server JDBC Driver and Spring Data JPA. Added connectivity configuration, ran integration tests via TDD, and proved connection to local SQL Server instance.
- **User Domain Model (TASK-004)**: Implemented `User` entity, `UserRole` and `UserStatus` enums, and `UserRepository`. Verified mapping and persistence with `users` table via `ddl-auto=update` and integration tests.
- **User Registration (TASK-005)**: Added registration page (`/register`), `RegisterRequest` DTO validation, `UserService` for business logic, duplicate email prevention, BCrypt password hashing, and default role (`CUSTOMER`) / status (`ACTIVE`). Evaluated with `RegisterControllerTest` and `UserServiceTest`.
- **Session Login (TASK-006)**: Added login page (`/login`), `AuthService` for authentication, session creation and management, logout functionality (`/logout`), BCrypt verification (`matches()`), logged-in navbar state, and TDD validations. Evaluated with `LoginControllerTest` and `AuthServiceTest`. Manual login/logout flow verified in browser.
- **Photographer Profile Domain Model (TASK-007)**: Implemented `PhotographerProfile` entity, `PhotographerVerificationStatus` enum, and `PhotographerProfileRepository`. Established `@OneToOne` unique mapping with `User`. Added `PhotographerProfileRepositoryIntegrationTests` to verify persistence and constraints.
- **Photographer Onboarding (TASK-008)**: Added `/become-photographer` onboarding flow with `PhotographerProfileRequest` DTO validation, `PhotographerProfileService` (transactional profile creation + role promotion), `PhotographerOnboardingController`, `photographer-onboarding.jsp` form, and `photographer-onboarding-status.jsp`. userId always sourced from session. Duplicate profile protection at service and DB levels. Session `userRole` updated to PHOTOGRAPHER on success without re-login.
- **Admin Photographer Approval (TASK-009)**: Added `/admin/photographers` list and detail views, approve (`PENDING → APPROVED`) and reject (`PENDING → REJECTED`) POST actions, `AdminPhotographerService` with `InvalidStatusTransitionException` for invalid transitions, session-based ADMIN role authorization. `UserServiceTest` cleanup fixed to delete child profiles before users (FK constraint).
- **Public Photographer Marketplace (TASK-010)**: Added `/photographers` listing and `/photographers/{id}` detail public routes (no login required). `PhotographerPublicDto` exposes only safe fields (no password, no email). `PublicPhotographerService` enforces APPROVED-only filtering at the repository/JPQL layer — not just in JSP. Navbar Photographers link and homepage "Explore Photographers" CTA now route to real pages. `fn` taglib used for JSTL-compatible EL expressions (substring, length). `findByIdAndVerificationStatusWithUser` query prevents status-bypass via direct URL guessing. `open-in-view=false` maintained; JOIN FETCH ensures User proxy is initialized before DTO mapping.

- **Photographer Portfolio with Cloudinary (TASK-011)**: Added approved-photographer image upload, owner-only deletion, SQL Server metadata persistence, private portfolio management, and public galleries on approved photographer detail pages. Image type/size validation and partial-failure cleanup are enforced in the service layer. Database and Cloudinary credentials are supplied only through environment variables. Automated Cloudinary behavior is verified with mocks; real Cloudinary browser verification remains pending.
- **Premium Photographer Marketplace + Search & Filter (TASK-012)**: Upgraded `/photographers` with dynamic search by keyword (name, bio, city), filter by city, minimum/maximum starting price, and minimum experience. Enforced strict server-side `APPROVED`-only visibility at the JPQL query layer. Implemented cover image resolution using Cloudinary portfolio assets with an elegant dark editorial fallback. Completely redesigned marketplace UI with a dark editorial aesthetic, custom shared design system (`photoconnect.css`), responsive filter panel, interactive cards with hover lift, and refined empty states.
- **Premium Photographer Booking Flow (TASK-013)**: Implemented customer-to-photographer booking creation flow. Allows an authenticated `CUSTOMER` to request a photoshoot session with an `APPROVED` photographer via `GET /photographers/{id}/book` and `POST /photographers/{id}/book`. Enforces price snapshotting (`agreedPrice` locked in from photographer's `priceFrom`), prevents self-booking (`customer.id != photographer.user.id`), blocks non-approved/inactive accounts, strictly isolates customer identity to server-side session `userId`, sets initial status `PENDING`, and provides a dark editorial confirmation page (`/bookings/{id}/success`) with ownership verification.

## Database Status
- SQL Server database `PhotoConnect` connectivity established.
- `users`, `photographer_profiles`, `portfolio_images`, and `bookings` tables mapped with foreign keys.
- **Verification**: `users`, `photographer_profiles`, and `bookings` table definitions match JPA entity models.

## Test Status
- 135 automated unit, service, controller, and DTO tests pass with 0 failures, 0 errors, and 0 skipped (2026-08-30).
- TDD approach strictly followed.

## How to Run the Project
- Run `mvn spring-boot:run` from the root directory to start the application on port 8080.
- Run `mvn package` to build the WAR artifact (`target/photoconnect.war`).
- Set `DB_USERNAME` and `DB_PASSWORD` before database-backed commands. Set the three Cloudinary environment variables documented in `docs/SETUP.md` before real uploads.

## Known Issues
- Real Cloudinary upload/delete browser verification is pending because credentials were unavailable.
- Runtime approved-photographer detail verification is pending because no approved profile existed in the local database.
- Human visual verification for marketplace search & filter and booking flow is pending.

## Next Task
- TASK-014: Booking Management & Photographer Accept/Reject Flow (not started).
