# PhotoConnect V5 — Master Progress Tracking Document

**Date:** 2026-10-25 (Asia/Saigon)  
**Branch:** `v5-master-development`  
**Checkpoint Baseline:** `3a6b19f` (checkpoint: V4 remediation)  
**Target Release:** PhotoConnect V5 Release Candidate  

Status values: `NOT_STARTED` | `IN_PROGRESS` | `IMPLEMENTED` | `TESTED` | `BLOCKED`

---

## Master Sprint & Phase Overview

| Phase | Description | Sprint | Status | Tests Passed | Files Changed |
|---|---|---|---|---|---|
| **Phase 0** | V5 Baseline & Stabilization | Sprint 1 | **TESTED** | 797 / 826 | Baseline docs, pom.xml |
| **Phase 1** | Identity, Email Verification & OTP | Sprint 2 | **TESTED** | 804 / 833 | 41 files changed |
| **Phase 2** | Professional User Profiles & Social Links | Sprint 2 | **TESTED** | 804 / 833 | 8 files changed |
| **Phase 3** | Photographer Onboarding & Verification | Sprint 2 | **TESTED** | 804 / 833 | 9 files changed |
| **Phase 4** | Complete Service Marketplace & Comparison | Sprint 3 | **TESTED** | 811 / 840 | 6 files changed |
| **Phase 5** | Advanced Discovery & Recommendations | Sprint 3 | **TESTED** | 811 / 840 | 6 files changed |
| **Phase 6** | Calendar & Booking Engine | Sprint 4 | **TESTED** | 822 / 851 | 1 file changed |
| **Phase 7** | Booking Detail & Shoot Brief | Sprint 5 | **TESTED** | 822 / 851 | 8 files changed |
| **Phase 8** | Deposit & Payment Consistency | Sprint 4 | **TESTED** | 822 / 851 | Verified |
| **Phase 9** | Messaging & STOMP Chat Collaboration | Sprint 4 | **TESTED** | 822 / 851 | Verified |
| **Phase 10** | Notification Center & Delivery | Sprint 4 | **TESTED** | 822 / 851 | Verified |
| **Phase 11** | Secure Photo Delivery & Revision Workflow | Sprint 5 | **TESTED** | 822 / 851 | Verified |
| **Phase 12** | Customer Workspace & Dashboard | Sprint 6 | **NOT_STARTED** | — | — |
| **Phase 13** | Photographer Studio & CRM | Sprint 6 | **NOT_STARTED** | — | — |
| **Phase 14** | Reviews, Trust & Reporting | Sprint 5 | **TESTED** | 822 / 851 | 11 files changed |
| **Phase 15** | Admin Management System & Auditing | Sprint 6 | **NOT_STARTED** | — | — |
| **Phase 16** | Complete UI/UX Polish (Midnight Blue) | Sprint 7 | **NOT_STARTED** | — | — |
| **Phase 17** | Performance, Query Optimization & Logging | Sprint 7 | **NOT_STARTED** | — | — |
| **Phase 18** | Security Hardening & Penetration Defense | Sprint 7 | **NOT_STARTED** | — | — |
| **Phase 19** | Database Migration & Baseline Verification | Sprint 8 | **NOT_STARTED** | — | — |
| **Phase 20** | Automated Testing & End-to-End QA | Sprint 8 | **NOT_STARTED** | — | — |
| **Phase 21** | Error Handling, Edge Cases & Resilience | Sprint 8 | **NOT_STARTED** | — | — |
| **Phase 22** | Academic Deliverables & Demo Environment | Sprint 8 | **NOT_STARTED** | — | — |

---

## Detailed Phase Status Log

### Phase 0: Baseline & Stabilization
- **Status:** **TESTED**
- **Existing Implementation:** Spring Boot 3.3.0, 33 controllers, 30 entities, 40 JSP views, 14 Flyway migrations, JWT security, private Cloudinary, booking state machine.
- **Missing Functionality in Baseline:** Email OTP authentication, Shoot Brief, package comparison, user reporting system, photographer CRM notes, comprehensive responsive audits.
- **Files Changed:**
  - `docs/PHOTOCONNECT_V5_BASELINE.md`
  - `docs/PHOTOCONNECT_V5_MASTER_PROGRESS.md`
- **Migration Changes:** None (V001–V021 verified).
- **Test Results:** 826 tests run, 797 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None in baseline.
- **Next Actionable Step:** Begin Phase 1 (Identity, Email Verification & OTP).

---

### Phase 1: Identity, Email Verification & Account Security
- **Status:** **TESTED**
- **Existing Implementation:** `RegisterController`, `LoginController`, `GoogleAuthController`, `User` entity with `UserStatus` (ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION), BCrypt password hashing.
- **Implemented & Verified:**
  - `spring-boot-starter-mail` dependency.
  - `EmailVerificationToken` entity and Flyway migration `V022__create_email_verification_tokens.sql`.
  - Cryptographically secure 6-digit OTP generation (`SecureRandom`, `%06d`, leading zeros preserved), BCrypt salted hash storage, 5-minute expiry, max 5 failed attempts lockout, 60s resend cooldown.
  - Rate limiting by account & source IP (`ClientIpUtils`).
  - Strict purposes: `REGISTRATION`, `PASSWORD_RESET`, `EMAIL_CHANGE`.
  - Local thread-safe mail capture mode (`CapturedEmail`) when external SMTP is unconfigured; zero OTP logging or plaintext storage.
  - Verification view (`verify-otp.jsp`), forgot password (`forgot-password.jsp`), reset password (`reset-password.jsp`).
  - Security management in `user-profile.jsp` (password change, authenticated email change challenge, account deactivation).
  - Profile URL sanitization enforcing HTTP/HTTPS and blocking `javascript:` / `data:` protocols.
- **Files Changed:**
  - `pom.xml`, `application.properties`, `UserStatus.java`, `ErrorCode.java`, `docs/ERROR_CODES.md`.
  - `EmailVerificationToken.java`, `EmailVerificationPurpose.java`, `EmailVerificationTokenRepository.java`.
  - `EmailService.java`, `EmailServiceImpl.java`, `EmailOtpService.java`, `EmailOtpServiceImpl.java`.
  - `UserService.java`, `UserServiceImpl.java`, `AuthServiceImpl.java`, `UserProfileServiceImpl.java`.
  - `RegisterController.java`, `EmailVerificationController.java`, `PasswordRecoveryController.java`, `AccountSecurityController.java`, `ClientIpUtils.java`.
  - `login.jsp`, `register.jsp`, `user-profile.jsp`, `verify-otp.jsp`, `forgot-password.jsp`, `reset-password.jsp`.
  - `EmailOtpServiceTest.java`.
- **Migration Changes:** `V022__create_email_verification_tokens.sql` (created and added to Flyway).
- **Test Results:** 833 tests run, 804 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.
- **Next Actionable Step:** Proceed to Phase 2 (Professional User Profiles) and Phase 3 (Photographer Onboarding & Verification).
- **Checkpoint Commit:** `eac8ae2`

---

### Phase 2: Professional User Profiles & Social Links
- **Status:** **TESTED**
- **Existing Implementation:** `PhotographerProfile` entity, `PhotographerProfileController`, `PhotographerProfileEditRequest`, basic portfolio link fields.
- **Implemented & Verified:**
  - Expanded `PhotographerProfile` entity with TikTok (`tiktok_url`) and YouTube (`youtube_url`) channels.
  - Updated `PhotographerProfileEditRequest` and form validation for social URLs.
  - Updated `photographer-detail.jsp` with responsive social icon badges linking to verified external platforms with `rel="noopener noreferrer"` and safe protocol validation (`http`/`https`).
  - Added starting price helper methods and responsive display consistency.
- **Files Changed:**
  - `src/main/java/com/photoconnect/entity/PhotographerProfile.java`
  - `src/main/java/com/photoconnect/dto/PhotographerProfileEditRequest.java`
  - `src/main/java/com/photoconnect/controller/PhotographerProfileController.java`
  - `src/main/webapp/WEB-INF/views/photographer-profile-edit.jsp`
  - `src/main/webapp/WEB-INF/views/photographer-detail.jsp`
- **Migration Changes:** Included in `V023__add_photographer_review_audit_and_social_urls.sql`.
- **Test Results:** 833 tests run, 804 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.
- **Next Actionable Step:** Sprint 3 (Phase 4 & 5).

---

### Phase 3: Photographer Onboarding & Verification
- **Status:** **TESTED**
- **Existing Implementation:** Basic photographer registration and admin list approval.
- **Implemented & Verified:**
  - Added audit metadata columns to `photographer_profiles`: `rejection_reason`, `admin_notes`, `reviewed_at`, `reviewed_by_admin_id`.
  - Rejection workflow: Admin rejection requires explicit reason.
  - Status display in `photographer-onboarding-status.jsp` clearly communicates rejection rationale and next steps to candidate.
  - Re-application workflow (`reapply`): Allows rejected photographers to revise bio, pricing, portfolio links, and re-submit for review (`REJECTED` -> `PENDING`).
  - Updated `AdminPhotographerController` and `admin-photographer-detail.jsp` with review audit trails, admin notes, and rejection forms while maintaining backward compatibility with existing tests.
- **Files Changed:**
  - `src/main/java/com/photoconnect/service/AdminPhotographerService.java`
  - `src/main/java/com/photoconnect/service/AdminPhotographerServiceImpl.java`
  - `src/main/java/com/photoconnect/controller/AdminPhotographerController.java`
  - `src/main/java/com/photoconnect/service/PhotographerProfileService.java`
  - `src/main/java/com/photoconnect/service/PhotographerProfileServiceImpl.java`
  - `src/main/java/com/photoconnect/controller/PhotographerOnboardingController.java`
  - `src/main/webapp/WEB-INF/views/photographer-onboarding.jsp`
  - `src/main/webapp/WEB-INF/views/photographer-onboarding-status.jsp`
  - `src/main/webapp/WEB-INF/views/admin-photographer-detail.jsp`
- **Migration Changes:** `V023__add_photographer_review_audit_and_social_urls.sql` (created and added to Flyway).
- **Test Results:** 833 tests run, 804 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.
- **Next Actionable Step:** Proceed to Phase 4 (Complete Service Marketplace & Comparison) & Phase 5 (Advanced Discovery & Recommendations).

---

### Phase 4: Complete Service Marketplace & Comparison
- **Status:** **TESTED**
- **Existing Implementation:** `ServicePackage` entity, `PhotographerPackageController`, `ServicePackageRepository`, basic package cards in `photographer-detail.jsp`.
- **Implemented & Verified:**
  - Enforced business rule: Maximum 10 active service packages per photographer in `ServicePackageServiceImpl.createPackage` and `togglePackageActive`.
  - Added `recommended` package detection flag in `ServicePackageDto` and `ServicePackageServiceImpl.getActivePackagesForPhotographer` (highlighting middle / recommended tier).
  - Side-by-side package comparison feature:
    - Added public API endpoint `GET /photographers/{id}/packages/compare?packageIds=1,2,3` in `PhotographerController` (validates max 3 packages, returns JSON).
    - Added comparison checkboxes on package cards in `photographer-detail.jsp`.
    - Added floating comparison dock indicating selected count (`compareDock`).
    - Added responsive side-by-side comparison modal (`packageComparisonModal`) comparing price, duration, photo deliverables, turnaround days, and inclusions with a direct booking CTA button.
  - Recommended package visual highlight with glowing Midnight Blue accent border and "Phổ biến nhất" badge.
  - Active packages count indicator vs max 10 in photographer studio dashboard (`photographer-packages.jsp`).
- **Files Changed:**
  - `src/main/java/com/photoconnect/dto/ServicePackageDto.java`
  - `src/main/java/com/photoconnect/service/ServicePackageServiceImpl.java`
  - `src/main/java/com/photoconnect/controller/PhotographerController.java`
  - `src/main/java/com/photoconnect/controller/PhotographerPackageController.java`
  - `src/main/webapp/WEB-INF/views/photographer-detail.jsp`
  - `src/main/webapp/WEB-INF/views/photographer-packages.jsp`
  - `src/test/java/com/photoconnect/service/ServicePackageServiceTest.java`
  - `src/test/java/com/photoconnect/controller/PhotographerControllerTest.java`
- **Migration Changes:** None required (fields already mapped).
- **Test Results:** 840 tests run, 811 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.
- **Next Actionable Step:** Sprint 4 (Phases 6, 8, 9, 10).

---

### Phase 5: Advanced Discovery & Recommendations
- **Status:** **TESTED**
- **Existing Implementation:** Multi-criteria keyword, city, price range, experience, and category filtering in `PhotographerController` and `PublicPhotographerServiceImpl`.
- **Implemented & Verified:**
  - Cold-start protection: Implemented exploration boost (+0.15) for newly approved photographers with 0 reviews in `calculateRecommendationScore`, preventing new creators from being buried at the bottom of search results.
  - Added `isNewTalent()` helper in `PhotographerPublicDto`.
  - Added "Mới tham gia" badge on photographer cards in `photographers.jsp` for cold-start creators.
  - Added quick budget range chips in `photographers.jsp`: `< 2 triệu`, `2 - 5 triệu`, `5 - 10 triệu`, `> 10 triệu`.
  - Added Vietnamese cities datalist for instant autocomplete suggestions in search bar (Hà Nội, TP. Hồ Chí Minh, Đà Nẵng, Cần Thơ, Hải Phòng, Nha Trang, Đà Lạt, Huế, Vũng Tàu, Hội An).
- **Files Changed:**
  - `src/main/java/com/photoconnect/dto/PhotographerPublicDto.java`
  - `src/main/java/com/photoconnect/service/PublicPhotographerServiceImpl.java`
  - `src/main/webapp/WEB-INF/views/photographers.jsp`
  - `src/test/java/com/photoconnect/service/PublicPhotographerServiceTest.java`
- **Migration Changes:** None.
- **Test Results:** 840 tests run, 811 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.
- **Next Actionable Step:** Proceed to Sprint 4 & Sprint 5.

---

### Phase 6: Calendar & Booking Engine
- **Status:** **TESTED**
- **Existing Implementation:** `ScheduleService`, `PhotographerAvailabilityController`, `PhotographerCalendarController`, slot collision logic.
- **Implemented & Verified:**
  - Enhanced `booking-form.jsp` with interactive availability slot fetcher from `/api/photographers/{profileId}/availability`.
  - Automatically fetches slots upon selecting booking date, rendering available and booked chips.
  - Clicking any available chip auto-populates the `#bookingTime` input with zero friction.
  - Verified `ScheduleService` interval collision algorithm (`newStart < existingEnd && newEnd > existingStart`) against active bookings, whole-day unavailabilities, and personal calendar events.
- **Files Changed:**
  - `src/main/webapp/WEB-INF/views/booking-form.jsp`
- **Migration Changes:** None.
- **Test Results:** 851 tests run, 822 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.

---

### Phase 7: Booking Detail & Shoot Brief
- **Status:** **TESTED**
- **Existing Implementation:** `Booking` entity, `BookingController`, `booking-detail.jsp`, `photographer-booking-detail.jsp`.
- **Implemented & Verified:**
  - Added Flyway migration `V024__add_booking_shoot_brief.sql` adding structured shoot brief fields: `concept_description`, `moodboard_url`, `backup_location`, `outfit_count`, `shot_list`, `props_needed`, `participant_count` to `dbo.bookings`.
  - Updated `Booking` entity, `BookingRequest` DTO, `BookingViewDto`, and `BookingServiceImpl.createBooking` to seamlessly capture, persist, and expose shoot brief data.
  - Enhanced `booking-form.jsp` with a collapsible "Shoot Brief & Ý tưởng sáng tạo" section (concept description, moodboard Pinterest/Drive URL, backup location, outfit count, shot list, props, participants).
  - Enhanced `booking-detail.jsp` and `photographer-booking-detail.jsp` with dedicated Shoot Brief cards adhering to Midnight Blue typography and styling.
  - Added unit test `createBooking_withShootBrief_persistsShootBriefFields` in `BookingServiceTest`.
- **Files Changed:**
  - `src/main/resources/db/migration/V024__add_booking_shoot_brief.sql`
  - `src/main/java/com/photoconnect/entity/Booking.java`
  - `src/main/java/com/photoconnect/dto/BookingRequest.java`
  - `src/main/java/com/photoconnect/dto/BookingViewDto.java`
  - `src/main/java/com/photoconnect/service/BookingServiceImpl.java`
  - `src/main/webapp/WEB-INF/views/booking-form.jsp`
  - `src/main/webapp/WEB-INF/views/booking-detail.jsp`
  - `src/main/webapp/WEB-INF/views/photographer-booking-detail.jsp`
  - `src/test/java/com/photoconnect/service/BookingServiceTest.java`
- **Migration Changes:** `V024__add_booking_shoot_brief.sql` (created and added to Flyway).
- **Test Results:** 851 tests run, 822 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.

---

### Phase 8: Deposit & Payment Consistency
- **Status:** **TESTED**
- **Existing Implementation:** `DepositService`, `BookingPaymentController`, `VNPayService`, `DepositPaymentTest`.
- **Implemented & Verified:**
  - Verified `DepositService` 30% calculation (`DEPOSIT_RATE = new BigDecimal("0.30")`), row-level pessimistic locking (`findByBookingIdForUpdate`), and idempotent state transitions.
  - Verified demo payment UI and VNPay IPN signature verification flow.
- **Files Changed:** None (existing implementation robust & verified by unit + UI contract tests).
- **Migration Changes:** None.
- **Test Results:** 851 tests run, 822 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.

---

### Phase 9: Messaging & STOMP Chat Collaboration
- **Status:** **TESTED**
- **Existing Implementation:** `ChatService`, `WebSocketChatController`, STOMP broker, `chat.jsp`.
- **Implemented & Verified:**
  - Verified STOMP destination `/topic/booking/{bookingId}/chat` message broadcasting.
  - Verified booking participant security check preventing unauthorized users from accessing or sending messages in external booking rooms.
  - Verified unread message tracking and batch mark-as-read on active room focus.
- **Files Changed:** None (implementation verified).
- **Migration Changes:** None.
- **Test Results:** 851 tests run, 822 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.

---

### Phase 10: Notification Center & Delivery
- **Status:** **TESTED**
- **Existing Implementation:** `NotificationService`, `NotificationApiController`, `notifications.jsp`, `navbar.jsp`.
- **Implemented & Verified:**
  - Verified unread notifications count API (`/api/notifications/unread-count`).
  - Verified 30-second polling interval in `navbar.jsp` badge display.
  - Verified mark-all-read and mark-individual-read workflows.
- **Files Changed:** None (implementation verified).
- **Migration Changes:** None.
- **Test Results:** 851 tests run, 822 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.

---

### Phase 11: Secure Photo Delivery & Revision Workflow
- **Status:** **TESTED**
- **Existing Implementation:** `DeliveryService`, `DeliveryController`, `customer-delivery.jsp`, `photographer-delivery.jsp`.
- **Implemented & Verified:**
  - Verified photo delivery state lifecycle (`DELIVERED` -> `REVISION_REQUESTED` -> `ACCEPTED` -> `COMPLETED`).
  - Verified Cloudinary private authenticated delivery links with signed download URLs.
  - Verified revision feedback submission by customer and re-upload flow by photographer.
- **Files Changed:** None (implementation verified).
- **Migration Changes:** None.
- **Test Results:** 851 tests run, 822 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.

---

### Phase 14: Reviews, Trust & Reporting
- **Status:** **TESTED**
- **Existing Implementation:** `ReviewService`, `ReviewController`, `AdminReviewController`.
- **Implemented & Verified:**
  - Created Flyway migration `V025__create_user_reports.sql` creating `dbo.user_reports` with foreign keys to users, bookings, and resolution admin.
  - Created `ReportCategory` enum (`INAPPROPRIATE_BEHAVIOR`, `NO_SHOW`, `HARASSMENT`, `PAYMENT_DISPUTE`, `POOR_DELIVERY`, `COPYRIGHT_VIOLATION`, `OTHER`).
  - Created `ReportStatus` enum (`PENDING`, `INVESTIGATING`, `RESOLVED`, `DISMISSED`).
  - Created `UserReport` entity and `UserReportRepository`.
  - Created `CreateReportRequest` DTO and `ReportDto`.
  - Implemented `ReportService` and `ReportServiceImpl` with validation, authorization check, and admin resolution workflow (`resolveReport`, `dismissReport`).
  - Added dispute reporting endpoint `POST /bookings/{id}/report` in `BookingController`.
  - Updated `AdminReviewController` with Trust & Safety Violation Reports tab (`?tab=reports`) and resolution handlers (`/reports/{id}/resolve`, `/reports/{id}/dismiss`).
  - Updated `booking-detail.jsp` with modal `#reportIssueModal` allowing customer to report incidents with structured categories and evidence description.
  - Updated `admin-reviews.jsp` with a moderation queue tab for Violation Reports, displaying reporter, reported user, booking link, category badges, and resolution actions while strictly preserving the 42 content page layout contract.
  - Added comprehensive unit tests in `ReportServiceTest` (10 tests covering creation, authorization, duplicate prevention, resolve, and dismiss).
- **Files Changed:**
  - `src/main/resources/db/migration/V025__create_user_reports.sql`
  - `src/main/java/com/photoconnect/entity/ReportCategory.java`
  - `src/main/java/com/photoconnect/entity/ReportStatus.java`
  - `src/main/java/com/photoconnect/entity/UserReport.java`
  - `src/main/java/com/photoconnect/repository/UserReportRepository.java`
  - `src/main/java/com/photoconnect/dto/CreateReportRequest.java`
  - `src/main/java/com/photoconnect/dto/ReportDto.java`
  - `src/main/java/com/photoconnect/service/ReportService.java`
  - `src/main/java/com/photoconnect/service/ReportServiceImpl.java`
  - `src/main/java/com/photoconnect/controller/BookingController.java`
  - `src/main/java/com/photoconnect/controller/AdminReviewController.java`
  - `src/main/webapp/WEB-INF/views/booking-detail.jsp`
  - `src/main/webapp/WEB-INF/views/admin-reviews.jsp`
  - `src/test/java/com/photoconnect/service/ReportServiceTest.java`
- **Migration Changes:** `V025__create_user_reports.sql` (created and added to Flyway).
- **Test Results:** 851 tests run, 822 passed, 29 skipped, 0 failures, 0 errors.
- **Unresolved Issues:** None.
- **Next Actionable Step:** Proceed to Sprint 6 — Management, CRM & Analytics (Phase 12: Customer Workspace & Dashboard, Phase 13: Photographer Studio & CRM, Phase 15: Admin Management System & Auditing).



