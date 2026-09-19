# PhotoConnect

PhotoConnect is a server-rendered photography marketplace for discovering approved photographers, requesting shoots, managing bookings, exchanging booking-scoped messages, completing a local demo deposit checkout, and publishing moderated reviews.

The project is an individual course application. Its frontend intentionally remains JSP/JSTL, HTML, CSS, and plain JavaScript; it does not use React, Vue, Angular, Python, an SPA architecture, or a Node frontend build pipeline.

## Main features

- Customer registration, BCrypt password hashing, session login, logout, and role-aware navigation
- Photographer onboarding with admin approval or rejection
- Public approved-photographer marketplace with search, filters, 12-item pagination, portfolio galleries, ratings, and reviews
- Cloudinary-backed portfolio upload and owner-only deletion
- Customer booking requests with server-snapshotted prices and availability validation
- Customer and photographer booking lifecycle management
- Booking-scoped STOMP/SockJS chat with SQL Server persistence and REST fallback
- Thirty-percent local demo deposit checkout, deterministic demo outcomes, result page, and printable receipt
- Completed-booking reviews with rating aggregation and admin hide/unhide moderation
- Admin dashboard, user status management, photographer moderation, booking monitoring, and review moderation
- Centralized MVC/JSON exception handling, validation, role checks, and ownership checks
- Optional, idempotent, non-destructive development seed data

## Technology stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 target, Spring Boot 3.3.0 |
| Web | Spring MVC, JSP/JSTL, HTML, CSS, plain JavaScript |
| Persistence | Spring Data JPA / Hibernate, Microsoft SQL Server |
| Realtime | Spring WebSocket, STOMP, SockJS |
| Images | Cloudinary |
| Authentication | Server-side HTTP session with BCrypt password verification |
| Build/test | Maven, JUnit 5, Mockito, Spring MVC Test |
| Packaging | WAR (`target/photoconnect.war`) |

## Architecture

```text
Browser
  -> JSP/JSTL + HTML/CSS/plain JavaScript
  -> Spring MVC / WebSocket controllers
  -> transactional services
  -> Spring Data repositories / JPA
  -> SQL Server
```

Cloudinary stores portfolio image binaries; SQL Server stores their metadata. STOMP/SockJS provides live booking chat, with an authenticated REST fallback. See [Architecture](docs/final/ARCHITECTURE.md) for details.

## Roles

- **Guest:** browse the homepage, marketplace, and approved photographer profiles.
- **Customer:** create and manage owned bookings, use demo checkout, chat with the assigned photographer, and review completed bookings.
- **Photographer:** submit a profile, manage an approved profile's portfolio and availability, and manage assigned booking requests and chat.
- **Admin:** view platform metrics and manage users, photographer applications, bookings, and review visibility.

## Requirements

- JDK 21 or newer (the Maven compiler target is Java 21)
- Maven 3.9+
- Microsoft SQL Server 2019+ and a database login with the required schema permissions
- PowerShell on Windows for the commands below
- Cloudinary credentials only when real portfolio upload/delete will be demonstrated

## Environment setup

Set configuration in the current PowerShell session. Use your own values; never commit credentials.

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=PhotoConnect;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="DB_USERNAME"
$env:DB_PASSWORD="DB_PASSWORD"
$env:WEBSOCKET_ALLOWED_ORIGINS="http://localhost:8080"
```

Optional portfolio configuration:

```powershell
$env:CLOUDINARY_CLOUD_NAME="CLOUDINARY_CLOUD_NAME"
$env:CLOUDINARY_API_KEY="CLOUDINARY_API_KEY"
$env:CLOUDINARY_API_SECRET="CLOUDINARY_API_SECRET"
```

The checked-in configuration contains placeholders/environment references only. `DB_URL` defaults to a local `PhotoConnect` database, but `DB_USERNAME` and `DB_PASSWORD` must be supplied.

## SQL Server and migrations

Create an empty database in SSMS if necessary:

```sql
CREATE DATABASE PhotoConnect;
GO
```

The development configuration currently uses `spring.jpa.hibernate.ddl-auto=update` for the base schema. Two forward-only manual SQL migrations are retained for databases created before their corresponding features:

1. `docs/development/migrations/V008__add_review_status.sql`
2. `docs/development/migrations/V010__professional_demo_payment.sql`

Apply only the scripts needed by the target database, in version order, using SSMS or `sqlcmd`. Back up important data first. Do not edit historical scripts after they have been applied. `V009` is an optional demo seed, not a schema migration.

## Optional demo seed

Two opt-in paths exist:

- **Full SQL demo dataset:** run `docs/development/seed/V009__demo_seed_data.sql` manually against `PhotoConnect`. It adds guarded demo accounts, photographer profiles, portfolio metadata, availability, bookings, deposits, reviews, and messages.
- **Application profile:** set a private `DEMO_PASSWORD`, then run with the `demo-seed` profile and the explicit enable flag. This path creates guarded demo accounts and approved photographer profiles only.

```powershell
$env:DEMO_PASSWORD="CHOOSE_A_PRIVATE_DEMO_PASSWORD"
mvn spring-boot:run "-Dspring-boot.run.profiles=demo-seed" "-Dspring-boot.run.arguments=--photoconnect.demo.seed-enabled=true"
```

Both paths are designed to be idempotent and non-destructive. They leave matching existing records unchanged. The database may therefore contain additional development data; seed documentation describes intended inserts, not a guaranteed total row count. See [TASK-028](docs/development/TASK-028-development-demo-data.md).

## Build, run, and test

```powershell
mvn test
mvn package
mvn spring-boot:run
```

Open `http://localhost:8080/`. The WAR is produced at `target/photoconnect.war`.

On Windows, stop every PhotoConnect Spring Boot/DevTools process before running `mvn clean`; an active process can lock files under `target/`.

## Project structure

```text
src/main/java/com/photoconnect/       controllers, services, repositories, entities, DTOs, configuration
src/main/resources/                   application configuration and static assets
src/main/webapp/WEB-INF/views/        JSP pages and shared fragments
src/test/java/com/photoconnect/       unit, MVC, repository, security, and UI contract tests
docs/development/                     task records, manual migrations, optional seed
docs/final/                           architecture, setup, demo, feature, and release-readiness documents
```

## Demo-payment disclosure

The checkout is a **Demo Payment Environment**. It always displays that **no real money will be transferred**. The server calculates the deposit as `agreedPrice x 30%`; no gateway, bank API, merchant account, or real transfer is connected. Card number, expiry, CVV, and cardholder input are transient request data and are not persisted.

## Known limitations

- Final human browser review at 1440 px, 1024 px, 768 px, and 390 px remains pending.
- Live multi-user WebSocket behavior requires two authenticated browser sessions and final rehearsal.
- Real Cloudinary upload/delete requires environment credentials and remains environment-dependent.
- SQL Server integration tests are skipped when their environment prerequisites are unavailable.
- The payment system is intentionally a local demonstration, not a production payment integration.
- The application uses application-managed session authorization rather than a full Spring Security filter-chain deployment.

For the complete Windows procedure, see [Setup Guide](docs/final/SETUP_GUIDE.md). For demonstration preparation, see [Demo Checklist](docs/final/DEMO_CHECKLIST.md) and [Demo Script](docs/final/DEMO_SCRIPT.md).
