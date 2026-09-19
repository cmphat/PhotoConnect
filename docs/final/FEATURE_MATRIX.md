# PhotoConnect Feature Matrix

Status vocabulary:

- **Implemented:** present in the current source.
- **Automated pass:** covered by the green Maven suite.
- **Manual pass/partial/pending:** based only on recorded human verification; automated coverage is not counted as a manual pass.

| Area | Implemented behavior | Automated status | Manual status |
|---|---|---|---|
| Authentication | Register, BCrypt hash, session login/logout, session rotation, account-status checks | Pass | Earlier login/logout pass recorded |
| Photographer onboarding | Customer submits one profile; role promotion; `PENDING` verification | Pass | Pending final rehearsal |
| Admin approval | Pending application list/detail; approve/reject transition | Pass | Earlier admin moderation pass recorded |
| Marketplace | Public approved/active photographer list and detail | Pass | Primary flow pass recorded |
| Portfolio | Approved-owner upload/delete; public gallery; Cloudinary metadata | Pass with mocked Cloudinary | Real Cloudinary browser check pending |
| Search/filter | Keyword, city, price range, and experience filters | Pass | Complex filter edge cases pending |
| Pagination | Database-backed, 12 cards per MVC page, validated page input | Pass | Primary pagination pass; edge cases pending |
| Booking | Customer-to-approved-photographer request, future date/time checks, price snapshot | Pass | Final browser rehearsal pending |
| Booking management | Customer cancellation; photographer accept/reject/complete state machine | Pass | `PENDING -> ACCEPTED` pass recorded; completion rehearsal pending |
| Availability | Owner-managed blocked dates; booking rejection for blocked dates | Pass | Browser block/unblock flow pending |
| Deposit/payment | Server-calculated 30% demo deposit, QR/card outcomes, locking, idempotency | Pass | Core demo flow pass; edge cases pending |
| Receipt | Owner-only result and printable receipt with legacy null tolerance | Pass | Core receipt pass recorded |
| Realtime chat | Booking-participant STOMP/SockJS chat, persistence, REST fallback | Pass | Live two-session browser check pending |
| Reviews | One review per completed owned booking; rating recalculation | Pass | Submission/rating browser check pending |
| Admin dashboard | User, profile, booking, review, and simulated-deposit metrics | Pass | Earlier pass recorded |
| User management | Search/filter and guarded status update, including admin self-protection | Pass | Final browser rehearsal pending |
| Review moderation | Hide/unhide with visible-rating recalculation | Pass | Earlier pass recorded |
| Validation | Bean Validation and service boundaries for account/profile/booking/search/chat/review/upload input | Pass | Partial manual edge-case coverage |
| Authorization | Session roles plus service ownership/participant checks for booking, portfolio, payment, receipt, review, chat, admin | Pass | Cross-role/cross-owner browser matrix pending |
| Exception handling | Central MVC error view and JSON `ApiResponse` errors | Pass | Final browser error-page review pending |
| Demo data | Optional guarded SQL dataset and double-opt-in application seed | Pass | SQL seed/marketplace visibility pass recorded |
| Responsive UI | Shared design system across all 27 full-page JSPs and navigation fragments | Pass via static contracts | 1440/1024/768/390 px review pending |

## Scope exclusions

Service packages, categories, vouchers, notifications, production payment processing, and AI features are not implemented. They are not required by the frozen final feature set and are not represented as completed.
