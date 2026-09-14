# TASK-021 Development Notes: Admin Review Moderation & Dynamic Rating Recalculation

## Overview

TASK-021 completes the remaining Tuần 6 / Phase 8 MVP functionality from `docs/ROADMAP.md`, `docs/PLAN_BE.md`, and `docs/SPEC.md` by implementing **Admin Review Moderation**:
1. **Review Status Lifecycle**: Introduces `ReviewStatus` enum (`VISIBLE`, `HIDDEN`) on `Review` entity and database table `reviews`, defaulting to `VISIBLE`.
2. **Interactive Moderation Actions**: Implements secure administrative endpoints to hide (`POST /admin/reviews/{id}/hide`) and unhide/restore (`POST /admin/reviews/{id}/unhide`) customer reviews with contextual feedback alerts and status preservation.
3. **Dynamic Drift-Free Rating Recalculation**: Immediately and transactionally recalculates the photographer profile's `averageRating` and `reviewCount` from only active `VISIBLE` reviews upon any moderation event (hide or unhide), ensuring ratings accurately reflect public feedback.
4. **Public Profile Filtering**: Public photographer detail pages (`/photographers/{id}`) strictly exclude moderated (`HIDDEN`) reviews from client reviews lists and rating statistics.
5. **Customer Transparency**: Customers viewing their own booking detail (`/bookings/{id}`) can still see their submitted review with a clear moderation indicator (`Hidden by moderation`).
6. **Unified Editorial Moderation UI (`admin-reviews.jsp`)**: Upgrades the review monitoring table with status badges (`VISIBLE` / `HIDDEN`), multi-status filter tabs (`All Reviews`, `Visible`, `Hidden`), and interactive moderation action buttons with confirmation dialogs.

---

## 1. Architecture & Component Design

```
                      Admin Client (Session: userId, userRole=ADMIN)
                                           │
                                           ▼
                             AdminSecurityUtils (Guard)
                                           │
                                           ▼
                                 AdminReviewController
                                           │
                                           ▼
                                   AdminReviewService
                                           │
                       ┌───────────────────┴───────────────────┐
                       ▼                                       ▼
                ReviewRepository                         ReviewService
                       │                                       │
                       ▼                                       ▼
            SQL Server (reviews table)            PhotographerProfileRepository
         (status: VISIBLE / HIDDEN)          (recalculate averageRating & reviewCount)
```

### Endpoints & Routes

| HTTP Method | Route | Controller | Description | Access Control |
|---|---|---|---|---|
| `GET` | `/admin/reviews` | `AdminReviewController` | Review list with status filter (`ALL`, `VISIBLE`, `HIDDEN`) and counts | `ADMIN` only |
| `POST` | `/admin/reviews/{id}/hide` | `AdminReviewController` | Hides review and recalculates photographer rating metrics | `ADMIN` only |
| `POST` | `/admin/reviews/{id}/unhide` | `AdminReviewController` | Restores review to visible and recalculates rating metrics | `ADMIN` only |
| `POST` | `/admin/reviews` | `AdminReviewController` | Disallowed root POST (returns HTTP 405 Method Not Allowed) | `ADMIN` only |

---

## 2. Business Rules & Security Protections

### 1. Centralized Session-Based Role Authorization
- All `/admin/reviews/**` endpoints enforce `AdminSecurityUtils.requireAdmin(session)`.
- Unauthenticated requests are immediately redirected to `/login`.
- Non-admin authenticated accounts (`CUSTOMER`, `PHOTOGRAPHER`) are redirected to `/`.
- Browser-submitted role parameters are never trusted.

### 2. Rating Drift Prevention & Dynamic Recalculation
- Review rating calculations (`AVG(1.0 * r.rating)` and `COUNT(r)`) strictly filter `WHERE r.status = 'VISIBLE'`.
- When an administrator hides a 1-star or 5-star review, the target photographer's `averageRating` and `reviewCount` are updated immediately in the same transaction.
- When an administrator restores a hidden review, the rating metrics are recalculated immediately.
- If all reviews for a photographer are hidden, the photographer's rating metrics gracefully reset to `0.0` rating and `0` review count ("New Artist" badge).

### 3. Public Visibility Isolation
- `ReviewRepository.findByPhotographerProfileIdWithCustomer` explicitly filters `AND r.status = 'VISIBLE'`, ensuring hidden reviews are never transmitted to public browsers or rendered on photographer detail pages.

### 4. Non-Destructive Moderation
- Moderating a review does not delete customer feedback or history from SQL Server.
- The review record remains intact with status `HIDDEN`, allowing audit trails and full administrative restoration if needed.

---

## 3. Database Schema Changes & Migration

### Migration Script: `docs/development/migrations/V008__add_review_status.sql`
A safe, non-destructive SQL Server script has been created:
```sql
USE PhotoConnect;
GO

IF NOT EXISTS (
    SELECT 1 
    FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.reviews') 
      AND name = 'status'
)
BEGIN
    ALTER TABLE dbo.reviews 
    ADD status VARCHAR(20) NOT NULL 
    CONSTRAINT DF_reviews_status DEFAULT 'VISIBLE';

    PRINT 'Added column status to reviews table with default VISIBLE.';
END
GO
```

---

## 4. Files Created & Modified

### Created Files
- `src/main/java/com/photoconnect/entity/ReviewStatus.java`: Enum defining `VISIBLE` and `HIDDEN`.
- `src/main/java/com/photoconnect/exception/ReviewNotFoundException.java`: Exception thrown when moderating non-existent reviews.
- `docs/development/migrations/V008__add_review_status.sql`: Safe SQL Server migration script for SSMS execution.
- `docs/development/TASK-021-admin-review-moderation.md`: Complete task documentation and verification specification.
- `src/test/java/com/photoconnect/entity/ReviewTest.java`: Unit tests for `Review` entity status lifecycle and constructors.
- `src/test/java/com/photoconnect/dto/ReviewDtoTest.java`: Unit tests for `ReviewDto` status mapping and helper methods.

### Modified Files
- `src/main/java/com/photoconnect/entity/Review.java`: Added `@Enumerated(EnumType.STRING)` `status` column defaulting to `ReviewStatus.VISIBLE`.
- `src/main/java/com/photoconnect/dto/ReviewDto.java`: Added `status` field, constructor overloads, and `isHidden()` accessor.
- `src/main/java/com/photoconnect/repository/ReviewRepository.java`: Filtered public and aggregate queries by `VISIBLE`; added `findByStatusWithDetails`, `findByIdWithDetails`, and `countByStatus`.
- `src/main/java/com/photoconnect/service/ReviewService.java` & `ReviewServiceImpl.java`: Exposed `recalculatePhotographerRating(Long profileId)`; updated `createReview` to set `ReviewStatus.VISIBLE`.
- `src/main/java/com/photoconnect/service/AdminReviewService.java` & `AdminReviewServiceImpl.java`: Implemented `listReviewsByStatus`, `hideReview`, `unhideReview`, `countByStatus`, and `countAll`.
- `src/main/java/com/photoconnect/controller/AdminReviewController.java`: Added status filtering (`?status=...`) and moderation endpoints (`POST /{id}/hide`, `POST /{id}/unhide`).
- `src/main/webapp/WEB-INF/views/admin-reviews.jsp`: Upgraded with status tabs (`All`, `Visible`, `Hidden`), status badges, alert banners, and interactive Hide/Unhide action buttons.
- `src/main/webapp/WEB-INF/views/booking-detail.jsp`: Added `(Hidden by moderation)` indicator when customer views their own moderated review.
- `docs/final/project-status.md`: Updated with TASK-020 PASS and TASK-021 PENDING status.
- `docs/ROADMAP.md`: Marked Tuần 6 "Hide review" completed.
- `docs/PLAN_BE.md`: Marked Phase 8 "Hide review" completed.
- `src/test/java/com/photoconnect/controller/AdminReviewControllerTest.java`: Expanded from 5 to 14 tests covering security, status filtering, hide/unhide actions, and error handling.
- `src/test/java/com/photoconnect/service/AdminReviewServiceTest.java`: Expanded from 1 to 11 tests covering all moderation methods and edge cases.
- `src/test/java/com/photoconnect/service/ReviewServiceTest.java`: Added tests for rating recalculation reset and status defaults.

---

## 5. Automated Testing & Verification

- **Entity & DTO Tests**:
  - `ReviewTest`: 5 tests verifying default status `VISIBLE`, constructor status assignment, setter mutation, and `@PrePersist` hook fallback.
  - `ReviewDtoTest`: 4 tests verifying `VISIBLE` mapping, `HIDDEN` mapping, `isHidden()` logic, and default constructor behavior.
- **Service Unit Tests**:
  - `AdminReviewServiceTest`: 11 tests covering:
    1. `listReviews` calls `findAllWithDetails`.
    2. `listReviewsByStatus(null)` falls back to all reviews.
    3. `listReviewsByStatus(VISIBLE)` calls `findByStatusWithDetails(VISIBLE)`.
    4. `listReviewsByStatus(HIDDEN)` calls `findByStatusWithDetails(HIDDEN)`.
    5. `hideReview` sets status `HIDDEN`, saves review, and triggers photographer rating recalculation.
    6. `hideReview` with null ID throws `IllegalArgumentException`.
    7. `hideReview` with non-existent ID throws `ReviewNotFoundException`.
    8. `unhideReview` sets status `VISIBLE`, saves review, and triggers photographer rating recalculation.
    9. `unhideReview` with null ID throws `IllegalArgumentException`.
    10. `unhideReview` with non-existent ID throws `ReviewNotFoundException`.
    11. `countByStatus` delegates correctly to repository.
  - `ReviewServiceTest`: 15 tests (+2 new tests verifying rating stats reset to `0.0` / `0` when no visible reviews remain, and accurate stats update with visible reviews).
- **Controller MVC Tests**:
  - `AdminReviewControllerTest`: 14 tests covering:
    1. Unauthenticated GET redirects to `/login`.
    2. Customer GET redirects to `/`.
    3. Photographer GET redirects to `/`.
    4. Admin GET returns reviews view with tab counts and active filter.
    5. Admin GET with `status=VISIBLE` filters visible reviews.
    6. Admin GET with `status=HIDDEN` filters hidden reviews.
    7. Unauthenticated POST to `/admin/reviews/{id}/hide` redirects to `/login`.
    8. Customer POST to `/admin/reviews/{id}/hide` redirects to `/`.
    9. Admin POST to `/admin/reviews/{id}/hide` hides review, adds flash message, and redirects.
    10. Admin POST hide preserves active status filter on redirect.
    11. Admin POST hide handles service exception gracefully with error flash message.
    12. Unauthenticated POST to `/admin/reviews/{id}/unhide` redirects to `/login`.
    13. Customer POST to `/admin/reviews/{id}/unhide` redirects to `/`.
    14. Admin POST to `/admin/reviews/{id}/unhide` unhides review, adds flash message, and redirects.
    15. Admin POST unhide preserves active status filter on redirect.
    16. Root `POST /admin/reviews` yields HTTP 405 Method Not Allowed (read-only root enforcement).
- **Full Suite Verification**:
  - `mvn test`: 320 tests run (298 passed, 0 failures, 0 errors, 22 skipped integration tests).
  - `mvn clean package`: BUILD SUCCESS (WAR artifact generated at `target/photoconnect.war`).

---

## 6. Human Verification Checklist (Status: PASS)

- [x] 1. Ensure the database schema has the `status` column on `reviews` (run `docs/development/migrations/V008__add_review_status.sql` in SSMS if `ddl-auto=update` has not applied it yet).
- [x] 2. Sign in as an `ADMIN` account.
- [x] 3. Navigate to `/admin/reviews`:
  - Verify page header displays "Review Moderation" with total review count.
  - Verify status filter tabs: `All Reviews (N)`, `Visible (V)`, `Hidden (H)`.
  - Verify table shows columns: `Review #`, `Status`, `Booking`, `Customer`, `Photographer`, `Rating`, `Comment`, `Submitted`, and `Moderation`.
  - Verify all existing reviews display the green `VISIBLE` badge.
- [x] 4. For a visible review, click the red "Hide" button and confirm the dialog:
  - Verify page redirects with green banner: *"Review #... has been hidden from public display."*
  - Verify the review row now shows the red `HIDDEN` badge and an "Unhide" button.
  - Verify tab counts update: Visible count decreases by 1, Hidden count increases by 1.
- [x] 5. Click the "Hidden" tab (`/admin/reviews?status=HIDDEN`):
  - Verify only hidden reviews are displayed.
- [x] 6. Navigate to the public photographer profile page (`/photographers/{id}`) whose review was hidden:
  - Verify the hidden review is NOT displayed in the "Client Reviews" list.
  - Verify the photographer's `averageRating` and review count have been updated to exclude the hidden review.
- [x] 7. Return to `/admin/reviews?status=HIDDEN`, click the green "Unhide" button, and confirm:
  - Verify page redirects with green banner: *"Review #... has been restored to public display."*
  - Verify review status reverts to `VISIBLE`.
- [x] 8. Return to the public photographer profile page (`/photographers/{id}`):
  - Verify the restored review is once again visible in the "Client Reviews" list.
  - Verify the photographer's rating and review count have been restored.
- [x] 9. Sign in as the `CUSTOMER` who authored the review and open `/bookings/{id}`:
  - When the review is `VISIBLE`: verify normal rating and review display.
  - When the review is `HIDDEN`: verify the `Hidden by moderation` notice appears next to the review date.
- [x] 10. Sign out, and attempt to send a POST to `/admin/reviews/{id}/hide`:
  - Verify immediate redirection to `/login`.
