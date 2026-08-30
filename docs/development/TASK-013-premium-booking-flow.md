# TASK-013 Development Notes: Premium Photographer Booking Flow

## Overview

TASK-013 introduces the direct customer-to-photographer booking flow in PhotoConnect. A logged-in `CUSTOMER` can browse an `APPROVED` photographer's profile, click **Book Photographer**, fill out session details (date, time, shoot location, and optional client notes), review the locked-in price snapshot (`agreedPrice`), and submit a booking request. The booking is stored in the `bookings` table in SQL Server with initial status `PENDING`.

---

## 1. Core Architecture & Entity Design

### `BookingStatus` Enum (`com.photoconnect.entity.BookingStatus`)
Represents the lifecycle states of a shoot booking:
- `PENDING` (Default initial state for all new requests created in TASK-013)
- `ACCEPTED` (To be managed by photographer in TASK-014)
- `REJECTED` (To be managed by photographer in TASK-014)
- `CANCELLED` (To be managed by customer/photographer in future tasks)
- `COMPLETED` (Final state upon session delivery)

Stored in database as `@Enumerated(EnumType.STRING)` (column `status VARCHAR(30)`).

### `Booking` Entity (`com.photoconnect.entity.Booking`)
Mapped to database table `bookings`:

| Column Name | Type | Constraints / Relations | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY IDENTITY | Unique booking identifier |
| `customer_id` | BIGINT | NOT NULL, FK → `users(id)` | Authenticated customer creating the booking |
| `photographer_profile_id` | BIGINT | NOT NULL, FK → `photographer_profiles(id)` | Target photographer profile |
| `booking_date` | DATE | NOT NULL | Scheduled shoot date (future or today) |
| `booking_time` | TIME | NOT NULL | Scheduled shoot time |
| `location` | VARCHAR(255) | NOT NULL | Shoot location / venue address |
| `notes` | VARCHAR(1000) | NULLABLE | Client requirements, outfits, concept references |
| `agreed_price` | DECIMAL(18,2) | NOT NULL | Snapshot of photographer's `priceFrom` at booking time |
| `status` | VARCHAR(30) | NOT NULL | Current status (`PENDING`) |
| `created_at` | DATETIME2 | NOT NULL, updatable = false | Timestamp when booking was created |
| `updated_at` | DATETIME2 | NULLABLE | Timestamp when booking was updated |

---

## 2. Business Rules & Security Protections

### 1. Price Snapshot (`agreedPrice`)
- When a customer books a photographer, the system reads `photographerProfile.getPriceFrom()` at that exact moment and copies it to `booking.agreedPrice`.
- If the photographer later changes their starting price from `500,000 VND` to `800,000 VND`, previously submitted bookings retain their agreed snapshot of `500,000 VND`.
- Stored as `BigDecimal` (`precision = 18, scale = 2`) to ensure monetary precision.

### 2. Session Identity & Non-Tampering
- Customer identity is **always** obtained from the server-side HTTP session (`(Long) session.getAttribute("userId")`).
- The client cannot supply or alter `customerId`, `agreedPrice`, or `status` through the browser.

### 3. APPROVED-Only Booking
- Only photographers with `verificationStatus = APPROVED` and an `ACTIVE` user account can receive booking requests.
- Booking non-approved or inactive profiles is rejected with `InvalidBookingException`.

### 4. Self-Booking Protection
- A photographer cannot book their own profile (`customer.id == photographerProfile.user.id`).
- Attempted self-bookings throw `SelfBookingNotAllowedException` and display a warning on the booking form.

### 5. Date Validation
- Booking dates must be in the present or future (`@FutureOrPresent` and service-level validation `!bookingDate.isBefore(LocalDate.now())`).
- Booking time and location are strictly required.

### 6. Customer Ownership & Authorization
- The booking confirmation/status route (`GET /bookings/{id}/success`) verifies that `booking.customer.id == session.userId`.
- Unauthorized users attempting to view another customer's booking are redirected to `/photographers`.

---

## 3. Layered Implementation Breakdown

### Service Layer (`BookingServiceImpl`)
- Annotated with `@Transactional` to guarantee atomic database operations.
- Coordinates verification across `UserRepository`, `PhotographerProfileRepository`, and `BookingRepository`.
- Exposes `createBooking(...)` and `getBookingForCustomer(...)`.

### Repository Layer (`BookingRepository`)
- Extends `JpaRepository<Booking, Long>`.
- Provides `findByCustomerIdOrderByCreatedAtDesc(Long customerId)`.
- Provides `findByPhotographerProfileIdOrderByCreatedAtDesc(Long profileId)`.
- Provides `findByIdWithDetails(Long id)` using `JOIN FETCH` on `customer`, `photographerProfile`, and `photographerProfile.user` to prevent `LazyInitializationException` under `spring.jpa.open-in-view=false`.

### Presentation & Controllers (`BookingController`)
- `GET /photographers/{id}/book`: Session-guarded route that loads photographer summary and renders `booking-form.jsp`.
- `POST /photographers/{id}/book`: Binds and validates `BookingRequest`, invokes `BookingService`, and applies the Post-Redirect-Get (PRG) pattern on success redirecting to `/bookings/{id}/success`.
- `GET /bookings/{id}/success`: Session-guarded route rendering `booking-success.jsp` for the booking owner.

### UI & Styling (`booking-form.jsp`, `booking-success.jsp`, `photoconnect.css`)
- Cohesive with the dark editorial aesthetic from TASK-012 (`#08080a`, `#101014`, champagne gold accents `#c9a96e`, serif headlines `Playfair Display`, modern sans `Inter`).
- Two-column responsive desktop layout (photographer rates & verified summary on left, clean form on right; stacked on mobile).
- Success page features glowing emerald confirmation indicator, structured details table, and navigation CTAs.

---

## 4. Automated Testing Results

- **DTO Validation Tests (`BookingRequestTest`)**: 6 unit tests passing.
- **Service Unit Tests (`BookingServiceTest`)**: 13 unit tests passing (covering valid booking, null price fallback, self-booking rejection, inactive accounts, pending/rejected profiles, past dates, and unauthorized customer access).
- **Controller MVC Tests (`BookingControllerTest`)**: 10 WebMvcTest tests passing (covering unauthenticated redirects, form loading, validation errors, self-booking feedback, success redirection, and ownership enforcement).
- **Full Suite**: 135 passing tests across all modules.

---

## 5. Human Verification Checklist (PENDING)

1. Log in as a `CUSTOMER` account (`/login`).
2. Navigate to `/photographers` and select an approved photographer.
3. On `/photographers/{id}`, click **📅 Book Photographer**.
4. On `/photographers/{id}/book`, select a future date, time, enter shoot location (e.g. "Ben Thanh Market"), and optional notes.
5. Click **Request Booking**.
6. Verify redirection to `/bookings/{bookingId}/success`.
7. Confirm that the success card shows status `● PENDING` and the exact photographer price snapshot.
8. Verify in SQL Server `SELECT * FROM dbo.bookings` that `status = 'PENDING'` and `agreed_price` matches.
9. Attempt self-booking (logging in as photographer and attempting to book own profile) to confirm clean rejection warning.
