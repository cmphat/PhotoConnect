# PhotoConnect V5 — Verified feature inventory

Baseline reviewed: branch `v5-master-development`, commit `82640c4cf588a82bf9eb8f9b36c80aa4aa37b3c1`.

Status meanings: **IMPLEMENTED** = executable path and supporting persistence/UI found; **PARTIALLY IMPLEMENTED** = usable core path with a material limitation; **DOCUMENTED ONLY** = claimed by documentation but not supported by executable source; **NOT IMPLEMENTED** = requested/expected capability absent; **UNVERIFIED** = source exists but an external dependency was not exercised during this documentation task.

| Area | Status | Source evidence | Notes |
|---|---|---|---|
| Public marketplace/search/profile/portfolio/package comparison | IMPLEMENTED | `PublicPhotographerController`, `PublicPhotographerServiceImpl`, marketplace JSPs | Guest-facing discovery path exists. |
| Local registration/login/logout | IMPLEMENTED | `RegistrationController`, `LoginController`, `AuthServiceImpl` | JWT cookie authentication is implemented. |
| Registration/password-reset/email-change OTP | PARTIALLY IMPLEMENTED | `EmailOtpServiceImpl`, `EmailServiceImpl`, `EmailVerificationToken` | SecureRandom six-digit OTP, BCrypt hash, expiry/attempt/rate limits exist; SMTP delivery is unverified and capture mode defaults on. |
| Native Google OAuth2/OIDC | UNVERIFIED | `GoogleOAuthSecurityConfig`, `GoogleOidcAuthenticationSuccessHandler`, `ExternalAuthIdentity` | Automated OIDC mapping tests pass; no live provider account was exercised. |
| Customer profile and avatar | IMPLEMENTED | `UserProfileController`, `UserProfileServiceImpl` | Public/self/edit/avatar routes exist. |
| Saved photographers | IMPLEMENTED | `SavedPhotographerController`, `SavedPhotographerServiceImpl` | Save/list/remove persisted. |
| Booking creation and package price snapshot | IMPLEMENTED | `BookingController`, `BookingServiceImpl`, `Booking` | Package name, price, duration, photo count and delivery days are snapshotted. |
| Booking status state machine and audit history | IMPLEMENTED | `BookingStateMachineServiceImpl`, `BookingHistory` | Explicit transitions and actor checks exist; legacy direct-completion paths remain deliberately supported. |
| Calendar availability and conflict validation | IMPLEMENTED | `ScheduleServiceImpl`, `BookingServiceImpl`, `BookingRepository` | Application checks and database locking paths exist. |
| Real payment gateway | NOT IMPLEMENTED | `DepositController`, `DepositServiceImpl`, `DemoPaymentMethod` | Deposit is explicitly simulated (`DEMO_QR`, `DEMO_CARD`). |
| Atomic simulated deposit settlement | IMPLEMENTED | `DepositServiceImpl`, `BookingStateMachineServiceImpl` | Paid deposit is required before confirmation; transactional service path exists. |
| Booking cancellation and rescheduling | IMPLEMENTED | `BookingController`, `PhotographerBookingController`, `RescheduleRequest` | Create/accept/reject/cancel flows exist. |
| Participant booking chat | IMPLEMENTED | `ChatController`, `ChatRestController`, `ChatWebSocketController`, `ChatServiceImpl` | Persistent messages and STOMP delivery exist. |
| Chat media attachments | NOT IMPLEMENTED | `Message` | Entity and endpoints support text content only. |
| Per-user notification inbox and STOMP delivery | IMPLEMENTED | `NotificationServiceImpl`, `NotificationController`, `WebSocketConfig` | Persisted inbox plus `/user/queue/notifications`. |
| Photographer profile/application approval | IMPLEMENTED | `PhotographerOnboardingController`, `AdminPhotographerController` | Actual lifecycle is `PENDING/APPROVED/REJECTED/SUSPENDED`; no draft/submitted states. |
| Portfolio/avatar Cloudinary media | UNVERIFIED | `CloudinaryServiceImpl`, `PortfolioServiceImpl`, `UserProfileServiceImpl` | Public upload/delete code exists; live Cloudinary was not exercised. |
| Private delivery upload and signed access | PARTIALLY IMPLEMENTED | `DeliveryServiceImpl`, delivery controllers | New delivery assets use authenticated upload and short-lived signed URLs; legacy public uploads cannot be retroactively made private without migration. |
| Delivery/revision/redelivery | PARTIALLY IMPLEMENTED | `DeliveryAlbumStatus`, `DeliveryServiceImpl` | Draft/delivered/revision-requested/accepted loop exists; no separate revision-number or revision-history entity. |
| Reviews and rating aggregation | IMPLEMENTED | `ReviewController`, `ReviewServiceImpl` | Completed-booking review and rating recalculation exist. |
| User reports/admin moderation | IMPLEMENTED | `ReportServiceImpl`, `ManagerOperationsServiceImpl`, moderation controllers | Manager assignment requires `PENDING -> INVESTIGATING`; Manager/Admin outcomes are limited to `RESOLVED` or `DISMISSED`. |
| Manager operational workspace | IMPLEMENTED | `ManagerController`, `ManagerOperationsServiceImpl`, manager JSPs | Dedicated permission-gated dashboard, paginated queues, approvals, reports, disputes and read-only audit view. |
| Support center | IMPLEMENTED | `SupportController`, `SupportTicketServiceImpl`, support entities/JSPs | Ownership-scoped requester history plus assigned Manager workflow. |
| Operational audit log | IMPLEMENTED | `OperationalAuditServiceImpl`, `OperationalAuditLog` | Records actor role/action/target/outcome; no ordinary mutation endpoint. |
| Admin accounts/photographers/bookings/reviews/analytics | IMPLEMENTED | admin controllers/services/JSPs | Role-protected console paths exist. |
| SQL Server/Flyway upgrade chain | PARTIALLY IMPLEMENTED | `db/migration/V001...V027` | V027 adds Manager operations and optimistic booking versioning; clean install remains blocked at unchanged V013 by SQL Server error 1785. |
| Category administration | NOT IMPLEMENTED | package/portfolio category fields only | Categories are values, not managed entities/workflows. |
| Production deployment topology | DOCUMENTED ONLY | application configuration and build descriptor | ARCH-08 is a source-derived logical deployment, not proof of an actual production environment. |

## Confirmed gaps and modelling constraints

- The prompt says “48 diagrams,” but its enumerated identifiers total **49**. All 49 identifiers are retained.
- No real payment processor is present; all payment diagrams explicitly say “mô phỏng.”
- External SMTP, Google OIDC and Cloudinary behavior is marked unverified where it needs credentials/network infrastructure.
- The full ERD shows physical relationships only where an entity association or migration constraint exists; scalar audit IDs are not presented as foreign keys.
- `ReportStatus.INVESTIGATING` is the required Manager assignment state before Manager resolution.
- The clean-install SQL Server incompatibility in V013 is documented in ERD and deployment/database notes; this documentation task did not run migrations.

