# TASK-013: Premium Photographer Booking Flow

## Goal
Implement the initial booking request workflow allowing an authenticated `CUSTOMER` to book an `APPROVED` photographer. The booking records customer identity (from session), photographer profile reference, booking date, booking time, shoot location, optional client notes, a snapshot of the starting price (`agreedPrice`), and initiates with status `PENDING`.

## Business Rules
1. Customer must be logged in with an active account (`UserStatus.ACTIVE`).
2. Photographer profile must exist and have `verificationStatus = APPROVED`.
3. Associated photographer user account must be `UserStatus.ACTIVE`.
4. Self-booking protection: A user cannot book their own photographer profile (`customer.id != photographerProfile.user.id`).
5. Price Snapshot: The photographer's current `priceFrom` is copied into `agreedPrice` at the time of booking creation so subsequent profile rate changes do not alter existing bookings.
6. Initial Status: All new bookings must strictly start with status `BookingStatus.PENDING`.
7. Booking date must be valid (must not be in the past).
8. Ownership Security: Customer identity is always resolved from session `userId`, never accepted as a request parameter. Booking confirmation/status views are strictly restricted to the customer who created the booking.

## Expected Files

### New Files
- `src/main/java/com/photoconnect/entity/BookingStatus.java`
- `src/main/java/com/photoconnect/entity/Booking.java`
- `src/main/java/com/photoconnect/dto/BookingRequest.java`
- `src/main/java/com/photoconnect/dto/BookingResponseDto.java` (safe presentation DTO)
- `src/main/java/com/photoconnect/exception/SelfBookingNotAllowedException.java`
- `src/main/java/com/photoconnect/exception/InvalidBookingException.java`
- `src/main/java/com/photoconnect/repository/BookingRepository.java`
- `src/main/java/com/photoconnect/service/BookingService.java`
- `src/main/java/com/photoconnect/service/BookingServiceImpl.java`
- `src/main/java/com/photoconnect/controller/BookingController.java`
- `src/main/webapp/WEB-INF/views/booking-form.jsp`
- `src/main/webapp/WEB-INF/views/booking-success.jsp`
- `src/test/java/com/photoconnect/dto/BookingRequestTest.java`
- `src/test/java/com/photoconnect/service/BookingServiceTest.java`
- `src/test/java/com/photoconnect/controller/BookingControllerTest.java`
- `src/test/java/com/photoconnect/repository/BookingRepositoryIntegrationTests.java`
- `process/tasks/TASK-013-premium-booking-flow.md`
- `docs/development/TASK-013-premium-booking-flow.md`

### Modified Files
- `src/main/webapp/WEB-INF/views/photographer-detail.jsp` (real Book Photographer CTA link)
- `src/main/webapp/assets/css/photoconnect.css` (booking UI components)
- `src/main/resources/static/assets/css/photoconnect.css`
- `docs/final/project-status.md`

## Checklist
- [x] inspect current architecture
- [x] design Booking entity
- [x] create BookingStatus enum
- [x] create Booking entity
- [x] create BookingRepository
- [x] create BookingRequest DTO
- [x] add validation
- [x] create BookingService
- [x] enforce authenticated user
- [x] prevent invalid photographer booking
- [x] prevent self-booking
- [x] create booking form controller
- [x] create booking POST controller
- [x] create booking success/status page
- [x] update photographer detail CTA
- [x] create tests
- [x] run regression tests
- [x] build WAR
- [x] runtime verify
- [x] write documentation
- [x] update final status
- [ ] await human browser/database verification

## Verification Commands
```powershell
mvn test "-Dtest=*Booking*Test"
mvn test
mvn package
```

## Problems Encountered & Solutions
1. **Windows target folder lock during WAR packaging**: On Windows, the IDE language server held an open lock on exploded test classes in `target/photoconnect-0.0.1-SNAPSHOT`. Resolved by configuring `maven-war-plugin` `webappDirectory` to `${project.build.directory}/photoconnect-webapp` and `<finalName>photoconnect</finalName>` in `pom.xml`.
2. **Offline database tests without active credentials**: Integration tests requiring live SQL Server connections were marked with `@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")` to ensure the full test suite runs cleanly in both offline development and database-connected CI/CD environments.

## Result
- **Automated Tests**: 135 passing tests (0 failures, 0 errors) across unit, service, controller, and DTO test suites.
- **Artifact**: `target/photoconnect.war` successfully packaged.
- **Human Verification**: PENDING browser / database verification.

## Status
AUTOMATED VERIFICATION COMPLETED / PENDING HUMAN VERIFICATION

## TDD Plan

### Slice 1: Enum, Entity & DTO Validation
- Create `BookingStatus` enum (`PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `COMPLETED`).
- Create `Booking` entity mapping to table `bookings`.
- Create `BookingRequest` DTO and `BookingRequestTest` to verify date/time/location validation.

### Slice 2: Exceptions, Repository & Service Layer
- Create `SelfBookingNotAllowedException` and `InvalidBookingException`.
- Create `BookingRepository` with query helpers.
- Write `BookingServiceTest` (RED) verifying:
  - Successful booking creation with `agreedPrice` snapshot and `PENDING` status.
  - Self-booking rejection.
  - Non-approved photographer rejection.
  - Inactive customer/photographer rejection.
  - Past date rejection.
  - Customer ownership retrieval check.
- Implement `BookingServiceImpl` (GREEN).

### Slice 3: Controller & Authorization
- Write `BookingControllerTest` (RED) verifying:
  - Unauthenticated GET `/photographers/{id}/book` redirects to `/login`.
  - Authenticated GET `/photographers/{id}/book` renders form.
  - Invalid POST re-renders form with errors.
  - Valid POST creates booking and redirects to `/bookings/{id}/success`.
  - Authorized customer can view success page; unauthorized customer receives 403/denial.
- Implement `BookingController` (GREEN).

### Slice 4: UI & Integration
- Update `photoconnect.css` with booking form, summary cards, and success page styles.
- Create `booking-form.jsp` and `booking-success.jsp`.
- Update `photographer-detail.jsp` CTA button.
- Create `BookingRepositoryIntegrationTests` for SQL Server persistence.

## Verification Commands
```powershell
mvn test "-Dtest=*Booking*Test"
mvn test "-Dtest=*ControllerTest,*ServiceTest,*Test,!UserServiceTest"
mvn test
mvn clean package
```

## Problems Encountered
(To be recorded as implementation progresses)

## Result
(To be recorded upon completion)

## Status
IN PROGRESS
