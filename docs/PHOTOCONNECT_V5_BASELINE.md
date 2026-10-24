# PhotoConnect V5 — Baseline & Stabilization Report

**Date:** 2026-10-25 (Asia/Saigon)  
**Branch:** `v5-master-development` (branched from commit `3a6b19f`)  
**Base Test Run:** 826 tests (797 passed, 29 skipped, 0 failures, 0 errors)  
**Architecture:** Spring Boot 3.3.0, Java 21, JSP/JSTL, SiteMesh, Microsoft SQL Server 16.0, Flyway 10.10.0, WebSocket/STOMP, Cloudinary  

---

## 1. Executive Summary

This baseline establishes the pre-V5 state of the PhotoConnect platform. The previous V4 remediation successfully resolved all release-blocking findings:
- JWT signing keys now require an external secret, enforce $\ge 256$-bit strength, fail fast on missing/weak secrets, and reject blacklisted keys.
- Cloudinary private media delivery fails closed and redacts raw storage URLs from customer DTOs.
- The delivery revision state machine authorizes customer revision requests (`DELIVERED -> POST_PROCESSING`) and restricts completion to customers (`DELIVERED -> COMPLETED`).
- Flyway migration runner (10.10.0) manages versioned migrations `V001` through `V021` with `ddl-auto=validate`.
- WebSocket STOMP inbound channel interception enforces deny-by-default access control without circular dependencies.

The goal of V5 is to bring PhotoConnect from this secured core to a complete, full-featured, demonstrable marketplace product ready for academic review and operational staging.

---

## 2. Complete Inventory of Existing Components

### 2.1. Controllers (33 files)
- **Authentication & Security:**
  - `LoginController.java`: `/login`, `/logout` (session & JWT cookie management)
  - `RegisterController.java`: `/register` (basic customer registration)
  - `GoogleAuthController.java`: `/auth/google` (Supabase external auth integration)
  - `UserProfileController.java`: `/profile`, `/profile/edit`, `/profile/avatar`
- **Marketplace & Public:**
  - `HomeController.java`: `/` (curated homepage with featured photographers)
  - `PhotographerController.java`: `/photographers`, `/photographers/{id}` (public profile, gallery, packages, reviews)
  - `FeedController.java`: `/feed` (social photo exploration feed)
  - `SavedPhotographerController.java`: `/saved-photographers`, bookmarks
- **Customer Workspace & Bookings:**
  - `CustomerDashboardController.java`: `/dashboard` (overview, upcoming bookings)
  - `BookingController.java`: `/bookings`, `/bookings/new`, `/bookings/{id}`
  - `DepositController.java`: `/deposit/{bookingId}`, `/deposit/checkout`, `/deposit/confirm`
  - `CustomerDeliveryController.java`: `/bookings/{id}/delivery`, photo preview & download
  - `ReviewController.java`: `/reviews/new`, `/reviews`
- **Photographer Studio & Operations:**
  - `PhotographerDashboardController.java`: `/photographer/dashboard`, `/photographer/analytics`
  - `PhotographerOnboardingController.java`: `/photographer/onboard`
  - `PhotographerProfileController.java`: `/photographer/profile/edit`
  - `PhotographerPortfolioController.java`: `/photographer/portfolio`
  - `PhotographerPackageController.java`: `/photographer/packages`
  - `PhotographerScheduleController.java`: `/photographer/schedule`
  - `PhotographerBookingController.java`: `/photographer/bookings`, `/photographer/bookings/{id}`
  - `PhotographerDeliveryController.java`: `/photographer/bookings/{id}/delivery`
- **Real-Time Collaboration & Communication:**
  - `ChatController.java`: `/chat`, `/chat/{bookingId}`
  - `ChatApiController.java`: `/api/chat/{bookingId}/messages`
  - `WebSocketChatController.java`: STOMP message handler `/app/chat`
  - `NotificationController.java`: `/notifications`
  - `NotificationApiController.java`: `/api/notifications/unread-count`, `/api/notifications/mark-read`
  - `CalendarApiController.java`: `/api/calendar/events`
- **Administration:**
  - `AdminDashboardController.java`: `/admin/dashboard`
  - `AdminUserController.java`: `/admin/users`
  - `AdminPhotographerController.java`: `/admin/photographers`
  - `AdminBookingController.java`: `/admin/bookings`
  - `AdminReviewController.java`: `/admin/reviews`
  - `AppErrorController.java`: `/error`

### 2.2. Entities & Database Schema (30 entities, 14 migrations)
- `User.java`: `Users` table (id, email, password, full_name, phone, role, status, avatar, bio, social URLs)
- `PhotographerProfile.java`: `photographer_profiles` (user_id, display_name, bio, city, experience_years, price_from, verification_status)
- `ServicePackage.java`: `service_packages` (photographer_profile_id, name, price, duration_minutes, photo_count, delivery_days, description)
- `PortfolioImage.java`: `portfolio_images` (photographer_profile_id, image_url, public_id, title, category, is_primary)
- `PortfolioCategory.java`: `portfolio_categories` (name, slug, sort_order)
- `Booking.java`: `bookings` (customer_id, photographer_profile_id, package_id, agreed_price, booking_date, booking_time, duration_minutes, status)
- `BookingHistory.java`: `booking_histories` (booking_id, actor_id, from_status, to_status, note, created_at)
- `Deposit.java`: `deposits` (booking_id, amount, status, payment_method, transaction_reference, paid_at)
- `DeliveryAlbum.java`: `delivery_albums` (booking_id, photographer_profile_id, status, title, revision_notes, revision_count)
- `DeliveredPhoto.java`: `delivered_photos` (delivery_album_id, public_id, secure_url, original_filename, is_approved)
- `RescheduleRequest.java`: `reschedule_requests` (booking_id, requester_id, proposed_date, proposed_time, status)
- `Review.java`: `reviews` (booking_id, customer_id, photographer_profile_id, rating, comment, status)
- `Message.java`: `messages` (booking_id, sender_id, content, created_at, is_read)
- `Notification.java`: `notifications` (user_id, type, title, message, target_url, is_read)
- `CalendarEvent.java`: `calendar_events` (user_id, title, start_time, end_time, is_private)
- `SavedPhotographer.java`: `saved_photographers` (customer_id, photographer_profile_id)
- `ExternalAuthIdentity.java`: `external_auth_identities` (user_id, provider, provider_user_id, email)

### 2.3. Frontend JSP Views (40 files)
- Auth: `login.jsp`, `register.jsp`, `google-onboarding.jsp`
- Public: `index.jsp`, `feed.jsp`, `photographers.jsp`, `photographer-detail.jsp`
- Customer: `customer-dashboard.jsp`, `bookings.jsp`, `booking-form.jsp`, `booking-detail.jsp`, `booking-success.jsp`, `deposit.jsp`, `deposit-receipt.jsp`, `deposit-result.jsp`, `customer-delivery.jsp`, `review-form.jsp`, `saved-photographers.jsp`
- Photographer: `photographer-dashboard.jsp`, `photographer-analytics.jsp`, `photographer-onboarding.jsp`, `photographer-onboarding-status.jsp`, `photographer-profile-edit.jsp`, `photographer-portfolio.jsp`, `photographer-packages.jsp`, `photographer-schedule.jsp`, `photographer-bookings.jsp`, `photographer-booking-detail.jsp`, `photographer-delivery.jsp`
- Collaboration: `chat.jsp`, `notifications.jsp`, `user-profile.jsp`
- Admin: `admin-dashboard.jsp`, `admin-users.jsp`, `admin-photographers.jsp`, `admin-photographer-detail.jsp`, `admin-bookings.jsp`, `admin-booking-detail.jsp`, `admin-reviews.jsp`
- SiteMesh Decorators & Fragments: `default.jsp`, `admin.jsp`, `navbar.jsp`, `admin-navbar.jsp`, `csrf-input.jsp`

---

## 3. Feature Completeness Matrix

| Domain Module | V4 Baseline State | Missing in Baseline / Required for V5 | V5 Plan |
|---|---|---|---|
| **Identity & Auth** | Basic email/password registration; Google OAuth linking; JWT cookies | **Email OTP verification missing**; Password reset via OTP missing; Email change flow missing; Local mail capture missing | Phase 1 (Sprint 2) |
| **User Profiles** | Basic profile edit; Avatar upload; Social links | Social links lack safe HTTP/HTTPS strict validation & noopener; Calendar preview needs owner vs visitor privacy bounds | Phase 2 (Sprint 2) |
| **Photographer Onboarding** | Basic onboarding form; Admin approval toggle | Multi-stage status lifecycle (DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, SUSPENDED); Rejection reasons & audit history | Phase 3 (Sprint 2) |
| **Service Marketplace** | Service packages exist; Category filter exists | Package comparison UI missing; Snapshot preservation on booking creation needs explicit verification | Phase 4 (Sprint 3) |
| **Discovery & Search** | Keyword, city, specialty filtering | Transparent rule-based ranking (rating, reviews, bookings, completeness); Empty states & reset filters | Phase 5 (Sprint 3) |
| **Calendar & Scheduling** | Overlap check; FullCalendar integration | Working hours vs private events; Explicit reservation policies; Multi-threaded concurrency verification | Phase 6 (Sprint 4) |
| **Booking Workspace** | Booking detail shows package snapshot & status | Structured **Shoot Brief** (purpose, location notes, style references, participants) missing | Phase 7 (Sprint 5) |
| **Payment & Deposit** | Simulated checkout; Atomic deposit confirmation | Clear visual labeling of simulated transactions; Refund simulation audit records | Phase 8 (Sprint 4) |
| **Messaging & Chat** | WebSocket STOMP private chat; Interceptor security | Unread badge counts; Message length rate limits; Media attachment handling guidelines | Phase 9 (Sprint 4) |
| **Notification Center** | In-app notifications; Mark read; Unread count | Event coverage across all lifecycle changes; Email notification dispatch with local capture | Phase 10 (Sprint 4) |
| **Media Delivery & Handover** | Private Cloudinary upload; Redacted DTOs; Revision transition | Revision limits & tracking; Redelivery tracking; Review prompt trigger on delivery acceptance | Phase 11 (Sprint 5) |
| **Customer Workspace** | Customer dashboard overview; Next action hint | Unified pending requests list; Quick review shortcuts; Actionable empty states | Phase 12 (Sprint 6) |
| **Photographer Studio** | Studio dashboard; Analytics charts; Packages | Minimal booking CRM view (customer shoot history & private photographer notes) | Phase 13 (Sprint 6) |
| **Reviews & Trust** | Review creation; Admin moderation | User reporting system (inappropriate portfolio, abusive messages, dispute filing) | Phase 14 (Sprint 5) |
| **Admin Management** | Admin dashboard; Users, Photographers, Bookings | User dispute resolution; Content moderation audit logs; Category management | Phase 15 (Sprint 6) |
| **UI/UX Polish** | Midnight Blue tokens in place; SiteMesh layout | Viewport audits (375px, 768px, 1024px, 1440px); Vietnamese copy consistency; Accessibility tags | Phase 16 (Sprint 7) |

---

## 4. Current Test Suite Baseline

- **Total Tests:** 826
- **Passed:** 797
- **Skipped:** 29 (all gated behind `@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")` because teardown deletes violate foreign keys in shared database)
- **Failures:** 0
- **Errors:** 0
- **Execution Time:** ~17 seconds
