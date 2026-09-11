# TASK-019 Development Notes: Booking Reviews & Photographer Ratings

## Overview

TASK-019 implements a complete review and rating system directly linked to completed shoot bookings. The implementation provides:
1. Persistent database storage in SQL Server (`reviews` table).
2. Strict customer-ownership and booking lifecycle enforcement (`COMPLETED` status required, one review per booking).
3. Server-side derivation of customer and photographer identities from the `Booking` entity to prevent spoofing.
4. Drift-free rating statistics recalculation (`averageRating`, `reviewCount`) computed from persisted `Review` records and updated on `PhotographerProfile`.
5. Display of reviews and rating metrics on public photographer profiles and client booking detail views.
6. Minimal, accessible editorial UI (`review-form.jsp`) consistent with `photoconnect.css`.

---

## 1. Core Architecture & Entity Design

### `Review` Entity
Mapped to database table `reviews`:

| Column Name | Type | Constraints / Relations | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY IDENTITY | Unique identifier |
| `booking_id` | BIGINT | NOT NULL, UNIQUE, FK → `bookings(id)` | Associated booking (one review per booking) |
| `customer_id` | BIGINT | NOT NULL, FK → `users(id)` | Customer author |
| `photographer_profile_id` | BIGINT | NOT NULL, FK → `photographer_profiles(id)` | Target photographer |
| `rating` | INT | NOT NULL | Score from 1 to 5 |
| `comment` | NVARCHAR(1000) | NULLABLE | Feedback text (trimmed, max 1000 chars) |
| `created_at` | DATETIME2 | NOT NULL | Creation timestamp |
| `updated_at` | DATETIME2 | NULLABLE | Update timestamp |

- **Unique Constraint**: `uq_reviews_booking_id` on `booking_id` prevents multiple reviews for the same booking.

### `PhotographerProfile` Entity Updates
Added rating summary fields:
- `average_rating`: `Double` (default `0.0`), representing the computed average rating.
- `review_count`: `Integer` (default `0`, NOT NULL), representing total reviews.

---

## 2. Business Rules & Protections

### 1. Booking Association & Customer Ownership
- A review can only be submitted for a valid existing `Booking`.
- Only the `CUSTOMER` who owns the booking (`booking.customer.id == session.userId`) can create a review (`ReviewAccessDeniedException`).
- Customer and photographer profile references are derived strictly from the `Booking` entity on the server side; request body user IDs are never trusted.

### 2. Status Enforcement
- Only bookings with status `COMPLETED` can be reviewed (`BookingNotCompletedException`). Pending, accepted, or cancelled bookings cannot be reviewed.

### 3. Uniqueness
- A booking may be reviewed at most once (`ReviewAlreadyExistsException`). If a customer tries to review an already-reviewed booking, they are redirected with a friendly message.

### 4. Validation & Length Limits
- Rating must be an integer between 1 and 5 inclusive (`InvalidReviewException`).
- Comments are trimmed; comments exceeding 1000 characters are rejected. Blank comments are allowed and persisted as `null`.

### 5. Rating Recalculation
- Upon review creation, the photographer profile's `reviewCount` and `averageRating` are recalculated from persisted `Review` records (`AVG(1.0 * r.rating)` and `COUNT(r)`), rounded to 2 decimal places, preventing any rating drift.

---

## 3. Layered Implementation Breakdown

### Service Layer (`ReviewService` / `ReviewServiceImpl`)
- `createReview(Long bookingId, Long sessionUserId, ReviewRequest request)`: Validates rules, persists the review, recalculates photographer rating metrics, and returns a view-safe `ReviewDto`.
- `getReviewByBookingId(Long bookingId, Long sessionUserId)`: Retrieves existing review for the booking, protecting privacy between session users.
- `getReviewsForPhotographer(Long photographerProfileId)`: Retrieves all reviews for an approved photographer ordered newest first with eager customer fetching (`JOIN FETCH r.customer`) to prevent N+1 issues and `LazyInitializationException` with `open-in-view=false`.
- `hasReviewForBooking(Long bookingId)`: Fast existence check.

### Presentation & Controllers
- `ReviewController`:
  - `GET /bookings/{id}/review`: Renders `review-form.jsp` if the customer owns the completed booking and no review exists yet.
  - `POST /bookings/{id}/review`: Validates inputs and creates the review, redirecting to `/bookings/{id}` with success flash attributes.
- `BookingController`:
  - Injects `ReviewService` and populates `review` in `viewCustomerBooking` (`GET /bookings/{id}`).
- `PhotographerController`:
  - Injects `ReviewService` and populates `reviews` in `photographerDetail` (`GET /photographers/{id}`).

### UI & Styling
- `review-form.jsp`: Minimal editorial layout with accessible radio buttons (1–5 stars) and comment textarea with validation error feedback.
- `booking-detail.jsp`: For `COMPLETED` bookings, displays a "Leave a Review" callout button if unreviewed, or shows the customer's submitted rating and comment if already reviewed.
- `photographer-detail.jsp`: Displays average rating and review count in the header meta and booking sidebar, and includes a "Client Reviews" section listing customer reviews below the portfolio gallery.

---

## 4. Automated Testing & Verification

- **DTO Validation Tests (`ReviewRequestTest`)**: 7 tests covering valid review requests with/without comment, null rating, ratings < 1, ratings > 5, overlong comments, and maximum bound comments.
- **Service Unit Tests (`ReviewServiceTest`)**: 13 tests covering:
  1. Customer can review own COMPLETED booking.
  2. Cannot review PENDING booking.
  3. Cannot review ACCEPTED booking.
  4. Cannot review another customer's booking.
  5. Cannot review same booking twice.
  6. Rating < 1 rejected.
  7. Rating > 5 rejected.
  8. Blank comment allowed and saved as null.
  9. Overlong comment rejected (> 1000 chars).
  10. Profile `averageRating` updated correctly.
  11. Profile `reviewCount` updated correctly.
  12. Retrieval by booking ID for authorized customer.
  13. Retrieval by booking ID for unauthorized user rejected.
  14. Retrieval of photographer reviews list.
- **MVC Controller Tests (`ReviewControllerTest`)**: 9 WebMvcTest tests covering:
  1. Unauthenticated GET redirects to `/login`.
  2. Wrong customer or invalid booking redirects to `/bookings`.
  3. Booking not completed redirects to `/bookings/{id}`.
  4. Review already exists redirects to `/bookings/{id}`.
  5. Valid completed booking renders `review-form`.
  6. Unauthenticated POST redirects to `/login`.
  7. Valid POST creates review and redirects with success message.
  8. Duplicate review POST redirects with error message.
  9. Validation errors return to form without creating review.
- **Integration with Existing Controller Tests**:
  - `BookingControllerTest`: Verified `review` attribute population in `viewCustomerBooking`.
  - `PhotographerControllerTest`: Verified `reviews` attribute population in public photographer detail view.
- **Full Suite**: 249 tests run with 0 failures, 0 errors, and 22 skipped integration tests.

---

## 5. Human Verification Checklist (Status: PENDING)

- [ ] 1. Log in as a `CUSTOMER` account.
- [ ] 2. Create a booking with an approved photographer.
- [ ] 3. Log in as that `PHOTOGRAPHER`, accept the booking, and transition it to `COMPLETED`.
- [ ] 4. Log back in as the `CUSTOMER` and navigate to `/bookings/{id}`.
- [ ] 5. Confirm the "Leave a Review" button appears in the Review & Rating card.
- [ ] 6. Click "Leave a Review" and verify the `/bookings/{id}/review` form renders.
- [ ] 7. Select 5 stars, enter a comment "Exceptional photoshoot experience!", and submit.
- [ ] 8. Confirm redirection back to `/bookings/{id}` with success flash message and submitted review displayed.
- [ ] 9. Try navigating back to `/bookings/{id}/review` and verify redirection with "A review has already been submitted for this booking."
- [ ] 10. Visit public photographer profile (`/photographers/{id}`) as a guest and verify:
  - Header meta displays `★ 5.0 (1 review)`.
  - Sidebar displays `Rating: ★ 5.0 (1)`.
  - "Client Reviews" section displays the review with customer name, 5 stars, date, and comment.
- [ ] 11. In another browser, log in as a different `CUSTOMER` and verify they cannot access `/bookings/{id}/review` for that booking.
