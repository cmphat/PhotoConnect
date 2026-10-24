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
| **Phase 6** | Calendar & Booking Engine | Sprint 4 | **NOT_STARTED** | — | — |
| **Phase 7** | Booking Detail & Shoot Brief | Sprint 5 | **NOT_STARTED** | — | — |
| **Phase 8** | Deposit & Payment Consistency | Sprint 4 | **NOT_STARTED** | — | — |
| **Phase 9** | Messaging & STOMP Chat Collaboration | Sprint 4 | **NOT_STARTED** | — | — |
| **Phase 10** | Notification Center & Delivery | Sprint 4 | **NOT_STARTED** | — | — |
| **Phase 11** | Secure Photo Delivery & Revision Workflow | Sprint 5 | **NOT_STARTED** | — | — |
| **Phase 12** | Customer Workspace & Dashboard | Sprint 6 | **NOT_STARTED** | — | — |
| **Phase 13** | Photographer Studio & CRM | Sprint 6 | **NOT_STARTED** | — | — |
| **Phase 14** | Reviews, Trust & Reporting | Sprint 5 | **NOT_STARTED** | — | — |
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
- **Next Actionable Step:** Proceed to Sprint 4 — Booking & Collaboration (Phase 6: Calendar & Booking Engine, Phase 8: Deposit & Payment Consistency, Phase 9: Messaging & STOMP Chat Collaboration, Phase 10: Notification Center & Delivery).


