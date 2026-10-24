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
| **Phase 1** | Identity, Email Verification & OTP | Sprint 2 | **NOT_STARTED** | — | — |
| **Phase 2** | Professional User Profiles & Social Links | Sprint 2 | **NOT_STARTED** | — | — |
| **Phase 3** | Photographer Onboarding & Verification | Sprint 2 | **NOT_STARTED** | — | — |
| **Phase 4** | Complete Service Marketplace & Comparison | Sprint 3 | **NOT_STARTED** | — | — |
| **Phase 5** | Advanced Discovery & Recommendations | Sprint 3 | **NOT_STARTED** | — | — |
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
- **Status:** **IN_PROGRESS**
- **Existing Implementation:** `RegisterController`, `LoginController`, `GoogleAuthController`, `User` entity with `UserStatus` (ACTIVE, INACTIVE, SUSPENDED), BCrypt password hashing.
- **Missing Functionality:**
  - `spring-boot-starter-mail` dependency.
  - OTP generation service: 6-digit cryptographically secure OTP, preserving leading zeros, 5-minute expiry, keyed hashing, single-use consumption, max 5 failed attempts, 60s resend cooldown.
  - Rate limiting by account & source IP.
  - Separate purposes: REGISTRATION, PASSWORD_RESET, EMAIL_CHANGE.
  - Local in-memory / capture mail mode when live SMTP is unconfigured.
  - Registration verification view (`verify-otp.jsp`), forgot password (`forgot-password.jsp`), reset password (`reset-password.jsp`), change email flow.
  - Account deactivation & password change in security settings.
- **Files Changed:** Pending implementation.
- **Migration Changes:** `V022__create_email_verification_tokens.sql`.
- **Test Results:** Pending.
- **Unresolved Issues:** None.
- **Next Actionable Step:** Add `spring-boot-starter-mail` to `pom.xml`, create `V022` migration, implement `EmailOtpService` and `EmailService`.
