# TASK-017 Development Notes: Photographer Availability and Scheduling

## Overview

TASK-017 introduces the ability for photographers to manage their availability by blocking out specific dates. When a customer attempts to book a photographer, the system checks the photographer's schedule to ensure the requested date is available.

---

## 1. Core Architecture & Entity Design

### `PhotographerUnavailableDate` Entity
Mapped to database table `photographer_unavailable_dates`:

| Column Name | Type | Constraints / Relations | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY IDENTITY | Unique identifier |
| `photographer_profile_id` | BIGINT | NOT NULL, FK → `photographer_profiles(id)` | Target photographer profile |
| `unavailable_date` | DATE | NOT NULL | The blocked date |
| `reason` | VARCHAR(255) | NULLABLE | Optional reason (e.g., Vacation) |

- **Constraint**: Unique constraint on `(photographer_profile_id, unavailable_date)` prevents duplicate blocked dates.

---

## 2. Business Rules & Protections

### 1. Date Validation
- Photographers cannot block dates in the past.
- The same date cannot be blocked multiple times by the same photographer.

### 2. Booking Enforcement
- During `BookingService.createBooking`, the system checks `ScheduleService.isDateAvailable`.
- If the date is blocked, a `PhotographerUnavailableException` is thrown, which the `BookingController` catches and returns as a user-friendly error on the booking form.

### 3. Authorization
- The `PhotographerScheduleController` ensures that only the logged-in photographer can view, add, or remove their own blocked dates by resolving their profile from the session `userId`.

---

## 3. Layered Implementation Breakdown

### Service Layer (`ScheduleService`)
- Provides `addUnavailableDate`, `removeUnavailableDate`, `getUnavailableDates`, and `isDateAvailable`.
- Injected into `BookingServiceImpl` for pre-booking validation.

### Presentation & Controllers (`PhotographerScheduleController`)
- `GET /photographer/schedule`: Displays the management UI.
- `POST /photographer/schedule/add`: Blocks a date.
- `POST /photographer/schedule/remove/{id}`: Unblocks a date.

### UI & Styling
- `photographer-schedule.jsp`: Minimal editorial layout listing current blocked dates and providing a form with an `<input type="date">` to add new ones.
- Integrates cleanly with `photoconnect.css`.

---

## 4. Runtime Bug Discovered & Resolved

### Bug Report
During initial manual verification, opening `GET /photographer/schedule` resulted in an HTTP 500 Whitelabel Error Page:
```text
java.lang.ClassCastException: class java.lang.String cannot be cast to class com.photoconnect.entity.UserRole
    at com.photoconnect.controller.PhotographerScheduleController.getPhotographerProfileId(PhotographerScheduleController.java:40)
    at com.photoconnect.controller.PhotographerScheduleController.viewSchedule(PhotographerScheduleController.java:51)
```

### Root Cause
Across the entire application (`LoginController`, `PhotographerOnboardingController`, `navbar.jsp`, `AdminPhotographerControllerTest`, etc.), the session attribute `userRole` is stored and expected as a `String` (e.g. `"PHOTOGRAPHER"`, `"CUSTOMER"`, `"ADMIN"`):
```java
session.setAttribute("userRole", user.getRole().name()); // String
```
`PhotographerScheduleController.getPhotographerProfileId` had incorrectly attempted a direct cast `(UserRole) session.getAttribute("userRole")`, triggering `ClassCastException`.

### Exact Fix
Adhered strictly to the existing application-wide session contract:
1. Checked `session.getAttribute("userRole")` safely without casting:
   ```java
   Object role = session.getAttribute("userRole");
   if (role == null || !UserRole.PHOTOGRAPHER.name().equals(role.toString())) {
       return null;
   }
   ```
2. Added defensive null safety for `session == null` and `userId == null`.
3. Verified existing behavior: if `profileId == null` (e.g., unauthenticated, missing user ID, non-photographer role like `CUSTOMER` or `ADMIN`, or photographer without profile), the controller safely redirects to `/login`.

---

## 5. Automated Testing & Regression Coverage

### Regression Tests Added (`PhotographerScheduleControllerTest`)
Created `PhotographerScheduleControllerTest` with 11 test cases:
1. `viewSchedule_photographerSessionWithStringRole_shouldSucceedWithoutClassCastException`: Confirms that a session with `userRole` stored as `String` (`"PHOTOGRAPHER"`) renders `photographer-schedule` with model attributes and does not throw `ClassCastException`.
2. `viewSchedule_unauthenticated_shouldRedirectToLogin`: Missing session redirects to `/login`.
3. `viewSchedule_missingUserId_shouldRedirectToLogin`: Missing `userId` in session redirects to `/login`.
4. `viewSchedule_customerRole_shouldRedirectToLogin`: `CUSTOMER` session cannot access schedule, redirects to `/login`.
5. `viewSchedule_adminRole_shouldRedirectToLogin`: `ADMIN` session cannot access schedule, redirects to `/login`.
6. `viewSchedule_photographerWithoutProfile_shouldRedirectToLogin`: Photographer without profile redirects to `/login`.
7. `addUnavailableDate_photographerSessionWithStringRole_shouldSucceed`: Tests date blocking POST flow.
8. `addUnavailableDate_unauthenticated_shouldRedirectToLogin`: Unauthenticated date blocking rejected.
9. `addUnavailableDate_serviceThrowsIllegalArgumentException_shouldFlashErrorMessage`: Form validation/business rule exception flashes error.
10. `removeUnavailableDate_photographerSessionWithStringRole_shouldSucceed`: Unblock date POST flow.
11. `removeUnavailableDate_unauthenticated_shouldRedirectToLogin`: Unauthenticated remove rejected.

### Existing Tests Retained
- All 5 `ScheduleServiceTest` tests pass.
- All 23 `BookingServiceTest` tests pass, including availability check and `PhotographerUnavailableException`.
- Total suite: 192 tests pass with 0 failures, 0 errors.

### Build Verification
- `mvn clean package "-Dmaven.clean.failOnError=false"`: BUILD SUCCESS (produced `photoconnect.war`).

---

## 5.1 Language Consistency Pass (Standardization to English)

- **Root Cause of Mixed-Language Issue**: On `GET /photographer/schedule` and other date-rendering views, dates were formatted using the host operating system/JVM default locale (`vi_VN`), which rendered dates as Vietnamese strings (e.g., `"tháng 9 6, 2026"`).
- **Resolution**:
  1. Configured JVM default locale in `PhotoConnectApplication.java` (`Locale.setDefault(Locale.US)`).
  2. Added Spring Web locale properties in `application.properties` (`spring.web.locale=en_US`, `spring.web.locale-resolver=fixed`).
  3. Added explicit `<fmt:setLocale value="en_US" />` across all JSP views.
  4. Normalized all `LocalDate` and `LocalDateTime` instances to parse and format via JSTL (`pattern="MMM d, yyyy"`), yielding clean English output (e.g., `"Sep 6, 2026"`).
  5. Verified all navigation labels (`Home`, `Explore`, `Portfolio`, `Schedule`, `Booking Requests`, `Status`, `Account`, `Sign Out`) and UI text conform to standard English.
- **Verification**:
  - `mvn test`: 192 tests pass (0 failures, 0 errors).
  - `mvn clean package "-Dmaven.clean.failOnError=false"`: BUILD SUCCESS.

---

## 6. Human Verification Checklist (Status: PENDING)

- [x] 1. Log in as a `PHOTOGRAPHER` account.
- [x] 2. Navigate to "Schedule" in the navbar (`/photographer/schedule`).
- [x] 3. Verify page renders with 200 OK without `ClassCastException` and dates display in English.
- [ ] 4. Add a blocked date in the future.
- [ ] 5. Log out and log in as a `CUSTOMER`.
- [ ] 6. Navigate to the photographer's profile and attempt to book them on the blocked date.
- [ ] 7. Verify the system rejects the booking with "The photographer is not available on this date."
- [ ] 8. Log back in as the photographer and remove the blocked date.
- [ ] 9. Log in as the customer and successfully book the date.



