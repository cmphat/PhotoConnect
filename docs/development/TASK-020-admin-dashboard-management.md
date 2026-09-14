# TASK-020 Development Notes: Admin Dashboard & Management

## Overview

TASK-020 expands PhotoConnect's administrative capabilities into a unified, secure, and performant Administration Dashboard and Management suite using the project's existing architecture:
1. **Admin Dashboard (`/admin/dashboard`)**: High-level platform KPIs and operational metrics (users, customers, photographers, pending/approved verification counts, bookings by lifecycle state, reviews, and deposit counts/totals).
2. **Development Simulation Transparency**: Deposit metrics are explicitly marked as "Development Simulation (Sandbox)" to ensure simulated payment data from TASK-015 is never misrepresented as real platform revenue.
3. **User Management (`/admin/users`)**: Search by name/email, filter by `UserRole` and `UserStatus`, view user details, and update user status (`ACTIVE`, `INACTIVE`, `BANNED`) with server-side validation and self-protection guard (admins cannot ban/deactivate their own logged-in account).
4. **Photographer Management (`/admin/photographers`)**: Preserves and reuses the existing TASK-009 verification and approval logic while adding multi-status views (Pending Queue, Approved, Rejected, All) with rating metrics, review count, rate, and direct decision links.
5. **Read-Only Booking Monitoring (`/admin/bookings`)**: Centralized administrative monitoring of shoot bookings with status filtering (`ALL`, `PENDING`, `ACCEPTED`, `COMPLETED`, `CANCELLED`, `REJECTED`). Read-only; no arbitrary status manipulation.
6. **Read-Only Review Monitoring (`/admin/reviews`)**: Centralized administrative inspection of customer reviews and ratings with booking, customer, and photographer details. Read-only; no modification or deletion endpoints.
7. **Session Authorization & Security (`AdminSecurityUtils`)**: Centralized session authorization ensuring unauthenticated requests redirect to `/login` and non-admin authenticated users (`CUSTOMER`, `PHOTOGRAPHER`) redirect to `/`. No browser-submitted user or role parameters are trusted.
8. **Unified Editorial Navigation (`admin-navbar.jsp`)**: Reusable sub-navigation bar linking all administrative areas seamlessly within the PhotoConnect dark editorial design system.

---

## 1. Architecture & Component Design

```
                     Admin Client (Session: userId, userRole=ADMIN)
                                          │
                                          ▼
                            AdminSecurityUtils (Guard)
                                          │
        ┌───────────────────┬─────────────┴──────┬───────────────────┬───────────────────┐
        ▼                   ▼                    ▼                   ▼                   ▼
AdminDashboardController  AdminUserController  AdminPhotographer  AdminBookingController AdminReviewController
        │                   │                    │                   │                   │
AdminDashboardService     AdminUserService     AdminPhotographer  AdminBookingService    AdminReviewService
        │                   │                    │                   │                   │
        ├───────────────────┼────────────────────┼───────────────────┼───────────────────┤
        ▼                   ▼                    ▼                   ▼                   ▼
  UserRepository    PhotographerProfileRep   BookingRepository   DepositRepository   ReviewRepository
```

### Endpoints & Routes

| HTTP Method | Route | Controller | Description | Access Control |
|---|---|---|---|---|
| `GET` | `/admin` | `AdminDashboardController` | Redirects to `/admin/dashboard` | `ADMIN` only |
| `GET` | `/admin/dashboard` | `AdminDashboardController` | Renders platform KPIs & system analytics | `ADMIN` only |
| `GET` | `/admin/users` | `AdminUserController` | User management with keyword search & role/status filters | `ADMIN` only |
| `POST` | `/admin/users/{id}/status` | `AdminUserController` | Updates user status (`ACTIVE`, `INACTIVE`, `BANNED`) | `ADMIN` only (Self-protected) |
| `GET` | `/admin/photographers` | `AdminPhotographerController` | Photographer roster & verification queue | `ADMIN` only |
| `GET` | `/admin/photographers/{id}` | `AdminPhotographerController` | Detailed photographer application review | `ADMIN` only |
| `POST` | `/admin/photographers/{id}/approve` | `AdminPhotographerController` | Approves pending photographer | `ADMIN` only |
| `POST` | `/admin/photographers/{id}/reject` | `AdminPhotographerController` | Rejects pending photographer | `ADMIN` only |
| `GET` | `/admin/bookings` | `AdminBookingController` | Read-only booking monitoring with status filter | `ADMIN` only |
| `GET` | `/admin/reviews` | `AdminReviewController` | Read-only review and rating monitoring | `ADMIN` only |

---

## 2. Business Rules & Security Protections

### 1. Centralized Session-Based Role Authorization
- All `/admin/**` endpoints call `AdminSecurityUtils.requireAdmin(session)`.
- If `session.userId == null`: redirects to `/login`.
- If `session.userRole != 'ADMIN'`: redirects to `/`.
- Role parameters from request queries or form payloads are never trusted.

### 2. Self-Protection Guard for Administrators
- In `AdminUserService.updateUserStatus`, if `userId.equals(currentAdminId)`, the operation throws `IllegalStateException("Administrators cannot modify their own account status.")`.
- In `admin-users.jsp`, the status modification form is disabled and replaced with `"Protected (Self)"` for the logged-in administrator.

### 3. Simulation Data Transparency
- Development payment simulation data (TASK-015) is explicitly labeled as `Development Simulation Metrics` on `/admin/dashboard`.
- Prominent disclaimer banner explains: *"Deposit transactions and amounts displayed below represent simulated test data (TASK-015 sandbox). They do not represent real-world funds or actual platform revenue."*

### 4. Efficient Database Count & Aggregate Queries
- Zero full-table in-memory loading for dashboard statistics:
  - `userRepository.count()` & `userRepository.countByRole(...)`
  - `photographerProfileRepository.countByVerificationStatus(...)`
  - `bookingRepository.count()` & `bookingRepository.countByStatus(...)`
  - `reviewRepository.count()`
  - `depositRepository.countByStatus(...)` & `depositRepository.sumAmountByStatus(...)`
- Overview queries use JPQL `JOIN FETCH` inside `@Transactional(readOnly = true)` boundaries to prevent N+1 queries and guarantee compatibility with `open-in-view=false`.

### 5. Read-Only Monitoring Enforcement
- `/admin/bookings` and `/admin/reviews` expose only HTTP `GET` endpoints.
- Any attempt to send HTTP `POST`, `PUT`, or `DELETE` to `/admin/bookings` or `/admin/reviews` yields HTTP 405 Method Not Allowed.

---

## 3. Files Created & Modified

### Created Files
- `src/main/java/com/photoconnect/util/AdminSecurityUtils.java`: Centralized session authorization utility.
- `src/main/java/com/photoconnect/dto/AdminDashboardStatsDto.java`: DTO for dashboard metrics and simulated deposit amounts.
- `src/main/java/com/photoconnect/service/AdminDashboardService.java` & `AdminDashboardServiceImpl.java`: Dashboard statistics aggregation service.
- `src/main/java/com/photoconnect/service/AdminUserService.java` & `AdminUserServiceImpl.java`: User search and status management service.
- `src/main/java/com/photoconnect/service/AdminBookingService.java` & `AdminBookingServiceImpl.java`: Read-only booking monitoring service.
- `src/main/java/com/photoconnect/service/AdminReviewService.java` & `AdminReviewServiceImpl.java`: Read-only review monitoring service.
- `src/main/java/com/photoconnect/controller/AdminDashboardController.java`: Controller for `/admin` and `/admin/dashboard`.
- `src/main/java/com/photoconnect/controller/AdminUserController.java`: Controller for `/admin/users` and `/admin/users/{id}/status`.
- `src/main/java/com/photoconnect/controller/AdminBookingController.java`: Controller for `/admin/bookings`.
- `src/main/java/com/photoconnect/controller/AdminReviewController.java`: Controller for `/admin/reviews`.
- `src/main/webapp/WEB-INF/views/fragments/admin-navbar.jsp`: Sub-navigation bar across all admin sections.
- `src/main/webapp/WEB-INF/views/admin-dashboard.jsp`: Dashboard JSP with metrics, disclaimers, and shortcuts.
- `src/main/webapp/WEB-INF/views/admin-users.jsp`: User management JSP with search, filter, and status update actions.
- `src/main/webapp/WEB-INF/views/admin-bookings.jsp`: Booking monitoring JSP with lifecycle status filtering.
- `src/main/webapp/WEB-INF/views/admin-reviews.jsp`: Review monitoring JSP with star ratings and client comments.
- `src/test/java/com/photoconnect/service/AdminDashboardServiceTest.java`: Unit tests for KPI calculations.
- `src/test/java/com/photoconnect/controller/AdminDashboardControllerTest.java`: MVC tests for dashboard access and security.
- `src/test/java/com/photoconnect/service/AdminUserServiceTest.java`: Unit tests for user filtering and status updates.
- `src/test/java/com/photoconnect/controller/AdminUserControllerTest.java`: MVC tests for user management routes and validation.
- `src/test/java/com/photoconnect/service/AdminBookingServiceTest.java`: Unit tests for booking overview.
- `src/test/java/com/photoconnect/controller/AdminBookingControllerTest.java`: MVC tests for booking monitoring and read-only enforcement.
- `src/test/java/com/photoconnect/service/AdminReviewServiceTest.java`: Unit tests for review overview.
- `src/test/java/com/photoconnect/controller/AdminReviewControllerTest.java`: MVC tests for review monitoring and read-only enforcement.

### Modified Files
- `src/main/java/com/photoconnect/repository/UserRepository.java`: Added `countByRole` and `searchUsers` JPQL query.
- `src/main/java/com/photoconnect/repository/PhotographerProfileRepository.java`: Added `countByVerificationStatus` and `findAllWithUser` JPQL query.
- `src/main/java/com/photoconnect/repository/BookingRepository.java`: Added `countByStatus` and `findAllWithDetails` JPQL query.
- `src/main/java/com/photoconnect/repository/DepositRepository.java`: Added `countByStatus` and `sumAmountByStatus` aggregate query.
- `src/main/java/com/photoconnect/repository/ReviewRepository.java`: Added `findAllWithDetails` JPQL query.
- `src/main/java/com/photoconnect/service/AdminPhotographerService.java` & `AdminPhotographerServiceImpl.java`: Added `listAllPhotographers` and `listPhotographersByStatus`.
- `src/main/java/com/photoconnect/controller/AdminPhotographerController.java`: Enhanced `GET /admin/photographers` with status query filter while maintaining backward compatibility.
- `src/main/webapp/WEB-INF/views/admin-photographers.jsp`: Added `admin-navbar.jsp`, status filter tabs, ratings, review counts, and refined actions.
- `src/main/webapp/WEB-INF/views/fragments/navbar.jsp`: Updated Admin link to point to `/admin/dashboard`.
- `src/test/java/com/photoconnect/service/AdminPhotographerServiceTest.java`: Added tests for all/filtered photographer queries.
- `src/test/java/com/photoconnect/controller/AdminPhotographerControllerTest.java`: Added tests for status filtering.

---

## 4. Automated Testing & Verification

- **Dashboard Tests**:
  - `AdminDashboardServiceTest`: 1 test verifying accurate KPI counts and deposit sum aggregation.
  - `AdminDashboardControllerTest`: 6 tests verifying unauthenticated redirect, customer redirect, photographer redirect, admin root redirect, and admin dashboard rendering with statistics.
- **User Management Tests**:
  - `AdminUserServiceTest`: 5 tests verifying search delegation, normalized search, status update success, self-modification prevention, and invalid arguments.
  - `AdminUserControllerTest`: 6 tests verifying unauthenticated redirect, customer redirect, admin user listing, search/role/status filter parsing, status update success, invalid status handling, and self-modification rejection.
- **Photographer Management Tests**:
  - `AdminPhotographerServiceTest`: 11 tests (+3 new tests for `listAllPhotographers` and `listPhotographersByStatus`).
  - `AdminPhotographerControllerTest`: 11 tests (+2 new tests for `status=ALL` and `status=APPROVED`).
- **Booking Monitoring Tests**:
  - `AdminBookingServiceTest`: 2 tests verifying all bookings and filtered bookings retrieval.
  - `AdminBookingControllerTest`: 6 tests verifying unauthenticated redirect, customer redirect, photographer redirect, admin listing, status filtering, and HTTP 405 on mutating POST requests.
- **Review Monitoring Tests**:
  - `AdminReviewServiceTest`: 1 test verifying review retrieval with details.
  - `AdminReviewControllerTest`: 5 tests verifying unauthenticated redirect, customer redirect, photographer redirect, admin review listing, and HTTP 405 on mutating POST requests.
- **Full Suite Verification**:
  - `mvn test`: 288 tests run (266 passed, 0 failures, 0 errors, 22 skipped integration tests).
  - `mvn clean package`: BUILD SUCCESS (WAR artifact generated at `target/photoconnect.war`).

---

## 5. Human Verification Checklist (Status: PASS)

- [x] 1. Sign in as an `ADMIN` account.
- [x] 2. Verify navigation bar shows "Admin" pointing to `/admin/dashboard`.
- [x] 3. Access `/admin/dashboard` and verify KPI cards:
  - Platform Membership (Total Users, Customers, Photographers)
  - Verification & Quality (Pending Approvals, Approved Photographers, Client Reviews)
  - Bookings & Engagement (Total Bookings, Pending Bookings, Completed Shoots)
  - Deposit Simulation Metrics (Paid Deposits Count, Simulated Deposit Volume in VND)
  - Verify presence of the Development Simulation Notice banner.
- [x] 4. Click "Review Pending Applications" and verify it navigates to `/admin/photographers?status=PENDING`.
- [x] 5. Click "Users" in the admin sub-navigation (`/admin/users`):
  - Verify list of users with Full Name, Email, Role badge, Status badge, and Registered date.
  - Search by user name or email; verify matching results.
  - Filter by role (`CUSTOMER`, `PHOTOGRAPHER`, `ADMIN`); verify filtered results.
  - Filter by status (`ACTIVE`, `INACTIVE`, `BANNED`); verify filtered results.
  - For a non-admin user, change status from `ACTIVE` to `INACTIVE`; verify flash message and updated badge.
  - Verify the logged-in admin user row displays `(You)` and `Protected (Self)` with no status modification form.
- [x] 6. Click "Photographers" in the admin sub-navigation (`/admin/photographers`):
  - Switch between Pending Queue, Approved, Rejected, and All Photographers tabs.
  - Verify Display Name, User Email, City, Experience, Starting Rate, Rating stars, Review count, and Verification status.
  - For pending applications, click "Review & Decide", inspect details, and test Approve/Reject.
- [x] 7. Click "Bookings" in the admin sub-navigation (`/admin/bookings`):
  - Verify read-only table with Shoot Date, Time, Customer, Photographer, Location, Agreed Price, Status, and Created date.
  - Click status tabs (`ALL`, `PENDING`, `ACCEPTED`, `COMPLETED`, `CANCELLED`, `REJECTED`) and verify filtering.
  - Confirm there are no edit/cancel/status buttons (strictly read-only).
- [x] 8. Click "Reviews" in the admin sub-navigation (`/admin/reviews`):
  - Verify read-only table with Review #, Booking #, Customer, Photographer, Star Rating, Comment, and Submitted date.
  - Confirm there are no edit or delete options (strictly read-only).
- [x] 9. Sign out, and attempt to navigate directly to `/admin/dashboard`:
  - Verify immediate redirection to `/login`.
- [x] 10. Sign in as a `CUSTOMER` or `PHOTOGRAPHER`, and navigate to `/admin/dashboard`:
  - Verify immediate redirection to `/`.
