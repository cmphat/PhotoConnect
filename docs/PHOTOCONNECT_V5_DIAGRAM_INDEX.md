# PhotoConnect V5 — Diagram index

## Baseline and method

- Reviewed branch/commit: `v5-master-development` / `82640c4cf588a82bf9eb8f9b36c80aa4aa37b3c1`.
- Source of truth: controllers, services, repositories, JPA entities, Flyway SQL, security configuration and JSPs in this repository. Existing progress/release documents were treated as secondary evidence.
- The original package contained 49 requested diagrams. V5 Manager completion adds UC-05, ACT-10 and SITE-05 for **52** editable diagrams.
- Renderer: PlantUML `1.2024.7` from the Maven cache. All 52 sources are syntax checked and rendered as SVG/PNG after Manager updates.
- Shared visual style: [`diagrams/theme.puml`](diagrams/theme.puml). Feature status inventory: [`FEATURE_INVENTORY.md`](diagrams/00-feature-inventory/FEATURE_INVENTORY.md).

## Source reference legend

| Code | Primary source inspected |
|---|---|
| AUTH | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService`, `AuthServiceImpl`, authentication/account controllers |
| BOOK | `BookingController`, `PhotographerBookingController`, `BookingServiceImpl`, `BookingStateMachineServiceImpl`, booking repositories/entities |
| DISC | `PublicPhotographerController`, `PublicPhotographerServiceImpl`, package/profile/portfolio repositories |
| MEDIA | `CloudinaryServiceImpl`, `CloudinaryStorageServiceImpl`, `ImageValidationUtils`, portfolio/delivery controllers and services |
| MSG | `WebSocketConfig`, `StompSecurityChannelInterceptor`, chat/notification controllers and services |
| OTP | `EmailOtpServiceImpl`, `EmailServiceImpl`, verification/recovery controllers, `EmailVerificationToken` |
| ADMIN | admin controllers/services, `ReportServiceImpl`, review/report entities |
| MANAGER | `ManagerController`, `ManagerOperationsServiceImpl`, support/audit services, Manager JSPs |
| DATA | all JPA entities and `src/main/resources/db/migration/V001...V027` |
| VIEW | MVC `@RequestMapping` methods and `src/main/webapp/WEB-INF/jsp/**` |

Every entry below is **VERIFIED (source + syntax + render)** unless its explanation explicitly calls out an external-runtime limitation.

## Use case diagrams

| ID | Title / purpose | Refs | Editable source | Exports | Giải thích ngắn |
|---|---|---|---|---|---|
| UC-00 | Overall System — phạm vi và tác nhân | AUTH, BOOK, DISC, MEDIA, MSG, OTP, ADMIN | [PUML](diagrams/01-use-cases/UC-00-overall-system.puml) | [SVG](diagrams/exports/UC-00.svg) · [PNG](diagrams/exports/UC-00.png) | Tổng hợp đúng các vai trò và ba nhà cung cấp ngoài; payment được ghi rõ là mô phỏng. |
| UC-01 | Guest — chức năng công khai | DISC, AUTH, OTP, VIEW | [PUML](diagrams/01-use-cases/UC-01-guest.puml) | [SVG](diagrams/exports/UC-01.svg) · [PNG](diagrams/exports/UC-01.png) | Mô tả duyệt/tìm kiếm, đăng ký, đăng nhập, OTP và Google OAuth. |
| UC-02 | Customer — quy trình khách hàng | BOOK, MEDIA, MSG, DISC | [PUML](diagrams/01-use-cases/UC-02-customer.puml) | [SVG](diagrams/exports/UC-02.svg) · [PNG](diagrams/exports/UC-02.png) | Bao quát booking, deposit, chat, delivery, revision, review và notification. |
| UC-03 | Photographer — vận hành studio | BOOK, MEDIA, MSG, VIEW | [PUML](diagrams/01-use-cases/UC-03-photographer.puml) | [SVG](diagrams/exports/UC-03.svg) · [PNG](diagrams/exports/UC-03.png) | Chỉ thể hiện profile/portfolio/package/schedule/delivery thực có trong mã. |
| UC-04 | Administrator — kiểm duyệt | ADMIN, BOOK, DATA | [PUML](diagrams/01-use-cases/UC-04-administrator.puml) | [SVG](diagrams/exports/UC-04.svg) · [PNG](diagrams/exports/UC-04.png) | Admin giám sát deposit mô phỏng, không quản trị cổng thanh toán thật. |
| UC-05 | Manager — vận hành có giới hạn | MANAGER, AUTH, BOOK | [PUML](diagrams/01-use-cases/UC-05-manager.puml) | [SVG](diagrams/exports/UC-05.svg) · [PNG](diagrams/exports/UC-05.png) | Thể hiện quyền vận hành và các ranh giới cấm với Admin/private data. |

## Architecture diagrams

| ID | Title / purpose | Refs | Editable source | Exports | Giải thích ngắn |
|---|---|---|---|---|---|
| ARCH-01 | High-Level Architecture — thành phần chính | AUTH, BOOK, MSG, MEDIA, OTP, DATA | [PUML](diagrams/02-architecture/ARCH-01-high-level.puml) | [SVG](diagrams/exports/ARCH-01.svg) · [PNG](diagrams/exports/ARCH-01.png) | Spring Boot/JSP/JPA/SQL Server cùng các tích hợp ngoài đã được xác nhận từ source/pom. |
| ARCH-02 | MVC Request Lifecycle — request/response | AUTH, DATA, VIEW | [PUML](diagrams/02-architecture/ARCH-02-mvc-lifecycle.puml) | [SVG](diagrams/exports/ARCH-02.svg) · [PNG](diagrams/exports/ARCH-02.png) | Theo luồng filter → controller → service → repository → JSP. |
| ARCH-03 | Application Layers — phụ thuộc lớp | AUTH, BOOK, MEDIA, MSG, DATA | [PUML](diagrams/02-architecture/ARCH-03-application-layers.puml) | [SVG](diagrams/exports/ARCH-03.svg) · [PNG](diagrams/exports/ARCH-03.png) | Phân biệt presentation, application/domain và infrastructure. |
| ARCH-04 | Authentication & JWT Security | AUTH | [PUML](diagrams/02-architecture/ARCH-04-auth-jwt.puml) | [SVG](diagrams/exports/ARCH-04.svg) · [PNG](diagrams/exports/ARCH-04.png) | Thể hiện JWT cookie, filter, role và ownership checks. |
| ARCH-05 | WebSocket Messaging | MSG | [PUML](diagrams/02-architecture/ARCH-05-websocket.puml) | [SVG](diagrams/exports/ARCH-05.svg) · [PNG](diagrams/exports/ARCH-05.png) | Subscription được kiểm tra theo user/participant trước khi broker phân phối. |
| ARCH-06 | Cloudinary Media Storage | MEDIA | [PUML](diagrams/02-architecture/ARCH-06-cloudinary.puml) | [SVG](diagrams/exports/ARCH-06.svg) · [PNG](diagrams/exports/ARCH-06.png) | Tách public portfolio/avatar khỏi authenticated delivery; cảnh báo legacy public. |
| ARCH-07 | Email OTP / SMTP | OTP | [PUML](diagrams/02-architecture/ARCH-07-email-otp.puml) | [SVG](diagrams/exports/ARCH-07.svg) · [PNG](diagrams/exports/ARCH-07.png) | Mã hóa OTP, expiry/attempt/rate limit và hai chế độ SMTP/capture. |
| ARCH-08 | Deployment — topology logic | AUTH, DATA, MEDIA, MSG, OTP | [PUML](diagrams/02-architecture/ARCH-08-deployment.puml) | [SVG](diagrams/exports/ARCH-08.svg) · [PNG](diagrams/exports/ARCH-08.png) | Đây là topology suy ra; hạ tầng production và credentials chưa được xác minh. |

## Entity relationship diagrams

| ID | Title / purpose | Refs | Editable source | Exports | Giải thích ngắn |
|---|---|---|---|---|---|
| ERD-01 | Full Database ERD | DATA | [PUML](diagrams/03-erd/ERD-01-full-database.puml) | [SVG](diagrams/exports/ERD-01.svg) · [PNG](diagrams/exports/ERD-01.png) | Toàn bộ 19 persistent entities và quan hệ; SVG nên dùng cho báo cáo để zoom. |
| ERD-02 | Identity & User Profile | DATA, AUTH | [PUML](diagrams/03-erd/ERD-02-identity-profile.puml) | [SVG](diagrams/exports/ERD-02.svg) · [PNG](diagrams/exports/ERD-02.png) | Không vẽ FK cho `reviewed_by_admin_id` vì migration chỉ có scalar column. |
| ERD-03 | Booking, Schedule & Payment | DATA, BOOK | [PUML](diagrams/03-erd/ERD-03-booking-schedule-payment.puml) | [SVG](diagrams/exports/ERD-03.svg) · [PNG](diagrams/exports/ERD-03.png) | Làm rõ package snapshot và deposit 1:0..1. |
| ERD-04 | Portfolio, Delivery & Reviews | DATA, MEDIA | [PUML](diagrams/03-erd/ERD-04-portfolio-delivery-reviews.puml) | [SVG](diagrams/exports/ERD-04.svg) · [PNG](diagrams/exports/ERD-04.png) | Phân biệt ảnh portfolio công khai và metadata ảnh delivery. |
| ERD-05 | Messaging, Notifications, Support & Moderation | DATA, MSG, ADMIN, MANAGER | [PUML](diagrams/03-erd/ERD-05-messaging-notifications-moderation.puml) | [SVG](diagrams/exports/ERD-05.svg) · [PNG](diagrams/exports/ERD-05.png) | Bổ sung support tickets/messages, Manager assignment và operational audit từ V027. |

## Activity diagrams

| ID | Title / purpose | Refs | Editable source | Exports | Giải thích ngắn |
|---|---|---|---|---|---|
| ACT-01 | Registration & OTP Verification | OTP, AUTH | [PUML](diagrams/04-activity/ACT-01-registration-otp.puml) | [SVG](diagrams/exports/ACT-01.svg) · [PNG](diagrams/exports/ACT-01.png) | Có cả validation, cooldown/rate-limit, sai/hết hạn và resend. |
| ACT-02 | Photographer Onboarding | ADMIN, VIEW | [PUML](diagrams/04-activity/ACT-02-photographer-onboarding.puml) | [SVG](diagrams/exports/ACT-02.svg) · [PNG](diagrams/exports/ACT-02.png) | Dùng đúng PENDING/APPROVED/REJECTED và reapply. |
| ACT-03 | Search & Package Selection | DISC, VIEW | [PUML](diagrams/04-activity/ACT-03-search-package.puml) | [SVG](diagrams/exports/ACT-03.svg) · [PNG](diagrams/exports/ACT-03.png) | Có nhánh không có kết quả và login trước booking. |
| ACT-04 | Booking Creation & Approval | BOOK | [PUML](diagrams/04-activity/ACT-04-booking-approval.puml) | [SVG](diagrams/exports/ACT-04.svg) · [PNG](diagrams/exports/ACT-04.png) | Conflict được kiểm tra lúc tạo và kiểm tra lại lúc chấp nhận. |
| ACT-05 | Deposit Simulation | BOOK | [PUML](diagrams/04-activity/ACT-05-deposit-simulation.puml) | [SVG](diagrams/exports/ACT-05.svg) · [PNG](diagrams/exports/ACT-05.png) | Không mô tả payment gateway; giữ đúng `DEMO_QR/DEMO_CARD`. |
| ACT-06 | Cancellation & Rescheduling | BOOK | [PUML](diagrams/04-activity/ACT-06-cancel-reschedule.puml) | [SVG](diagrams/exports/ACT-06.svg) · [PNG](diagrams/exports/ACT-06.png) | Bao gồm ownership, eligible state và slot conflict. |
| ACT-07 | Photo Delivery & Revision | MEDIA, BOOK | [PUML](diagrams/04-activity/ACT-07-delivery-revision.puml) | [SVG](diagrams/exports/ACT-07.svg) · [PNG](diagrams/exports/ACT-07.png) | Thể hiện authenticated upload, signed URL và vòng revision/redelivery. |
| ACT-08 | Review Submission | ADMIN, BOOK | [PUML](diagrams/04-activity/ACT-08-review.puml) | [SVG](diagrams/exports/ACT-08.svg) · [PNG](diagrams/exports/ACT-08.png) | Review chỉ sau completed booking, một review mỗi booking. |
| ACT-09 | Admin Moderation | ADMIN | [PUML](diagrams/04-activity/ACT-09-admin-moderation.puml) | [SVG](diagrams/exports/ACT-09.svg) · [PNG](diagrams/exports/ACT-09.png) | Gom bốn nhánh moderation thực tế; không thêm category administration. |
| ACT-10 | Manager Operations | MANAGER, AUTH, BOOK, DATA | [PUML](diagrams/04-activity/ACT-10-manager-operations.puml) | [SVG](diagrams/exports/ACT-10.svg) · [PNG](diagrams/exports/ACT-10.png) | Luồng quyền, approval/report/support/dispute, notification và audit. |

## Sequence diagrams

| ID | Title / purpose | Refs | Editable source | Exports | Giải thích ngắn |
|---|---|---|---|---|---|
| SEQ-01 | Authentication | AUTH, DATA | [PUML](diagrams/05-sequence/SEQ-01-authentication.puml) | [SVG](diagrams/exports/SEQ-01.svg) · [PNG](diagrams/exports/SEQ-01.png) | Browser → controller → service → repository → JWT cookie. |
| SEQ-02 | OTP Email Verification | OTP, DATA | [PUML](diagrams/05-sequence/SEQ-02-otp-verification.puml) | [SVG](diagrams/exports/SEQ-02.svg) · [PNG](diagrams/exports/SEQ-02.png) | Lời gọi `EmailServiceImpl` là đồng bộ; external SMTP chưa chạy thật. |
| SEQ-03 | Search Photographers | DISC, DATA | [PUML](diagrams/05-sequence/SEQ-03-search-photographers.puml) | [SVG](diagrams/exports/SEQ-03.svg) · [PNG](diagrams/exports/SEQ-03.png) | Theo đúng search/list/detail và active packages. |
| SEQ-04 | Booking Creation | BOOK, DATA | [PUML](diagrams/05-sequence/SEQ-04-booking-creation.puml) | [SVG](diagrams/exports/SEQ-04.svg) · [PNG](diagrams/exports/SEQ-04.png) | Có snapshot, history và notification. |
| SEQ-05 | Booking Acceptance & Conflict | BOOK, DATA | [PUML](diagrams/05-sequence/SEQ-05-accept-conflict.puml) | [SVG](diagrams/exports/SEQ-05.svg) · [PNG](diagrams/exports/SEQ-05.png) | Thể hiện transaction/lock và revalidation trước accept. |
| SEQ-06 | Deposit Payment Transaction | BOOK, DATA | [PUML](diagrams/05-sequence/SEQ-06-deposit-transaction.puml) | [SVG](diagrams/exports/SEQ-06.svg) · [PNG](diagrams/exports/SEQ-06.png) | PAID và booking CONFIRMED nằm trong transaction mô phỏng, có rollback path. |
| SEQ-07 | WebSocket Chat | MSG, DATA | [PUML](diagrams/05-sequence/SEQ-07-websocket-chat.puml) | [SVG](diagrams/exports/SEQ-07.svg) · [PNG](diagrams/exports/SEQ-07.png) | Xác thực STOMP subscription và persist message trước broadcast. |
| SEQ-08 | Cloudinary Upload | MEDIA, DATA | [PUML](diagrams/05-sequence/SEQ-08-cloudinary-upload.puml) | [SVG](diagrams/exports/SEQ-08.svg) · [PNG](diagrams/exports/SEQ-08.png) | Ví dụ public portfolio upload có binary validation và metadata persistence. |
| SEQ-09 | Private Photo Delivery | MEDIA, BOOK | [PUML](diagrams/05-sequence/SEQ-09-private-delivery.puml) | [SVG](diagrams/exports/SEQ-09.svg) · [PNG](diagrams/exports/SEQ-09.png) | Ownership precedes signed authenticated URL; cảnh báo legacy public. |
| SEQ-10 | Customer Revision Request | MEDIA, BOOK | [PUML](diagrams/05-sequence/SEQ-10-revision-request.puml) | [SVG](diagrams/exports/SEQ-10.svg) · [PNG](diagrams/exports/SEQ-10.png) | Album và booking được cập nhật cùng transaction; không có revision history riêng. |
| SEQ-11 | Notification Delivery | MSG, DATA | [PUML](diagrams/05-sequence/SEQ-11-notification-delivery.puml) | [SVG](diagrams/exports/SEQ-11.svg) · [PNG](diagrams/exports/SEQ-11.png) | Persisted inbox + per-user STOMP queue. |
| SEQ-12 | Admin Dispute Resolution | ADMIN, BOOK | [PUML](diagrams/05-sequence/SEQ-12-admin-dispute.puml) | [SVG](diagrams/exports/SEQ-12.svg) · [PNG](diagrams/exports/SEQ-12.png) | Chỉ cho DISPUTED → COMPLETED/CANCELLED theo state machine. |

## State diagrams

| ID | Title / purpose | Refs | Editable source | Exports | Giải thích ngắn |
|---|---|---|---|---|---|
| STATE-01 | Booking Lifecycle | BOOK | [PUML](diagrams/06-state/STATE-01-booking-lifecycle.puml) | [SVG](diagrams/exports/STATE-01.svg) · [PNG](diagrams/exports/STATE-01.png) | Sao chép đầy đủ adjacency map, kể cả legacy direct completion. |
| STATE-02 | Photographer Approval | ADMIN | [PUML](diagrams/06-state/STATE-02-photographer-approval.puml) | [SVG](diagrams/exports/STATE-02.svg) · [PNG](diagrams/exports/STATE-02.png) | SUSPENDED có trong enum nhưng không có transition service/controller. |
| STATE-03 | Email OTP Lifecycle | OTP | [PUML](diagrams/06-state/STATE-03-email-otp.puml) | [SVG](diagrams/exports/STATE-03.svg) · [PNG](diagrams/exports/STATE-03.png) | Các trạng thái là suy ra từ expiry/usedAt/attempts vì entity không có status enum. |
| STATE-04 | Delivery Album Lifecycle | MEDIA, BOOK | [PUML](diagrams/06-state/STATE-04-delivery-album.puml) | [SVG](diagrams/exports/STATE-04.svg) · [PNG](diagrams/exports/STATE-04.png) | DRAFT → DELIVERED → ACCEPTED hoặc revision loop. |
| STATE-05 | Reschedule Request Lifecycle | BOOK | [PUML](diagrams/06-state/STATE-05-reschedule-request.puml) | [SVG](diagrams/exports/STATE-05.svg) · [PNG](diagrams/exports/STATE-05.png) | Chỉ PENDING có outgoing transitions. |
| STATE-06 | User Report Lifecycle | ADMIN, MANAGER | [PUML](diagrams/06-state/STATE-06-user-report.puml) | [SVG](diagrams/exports/STATE-06.svg) · [PNG](diagrams/exports/STATE-06.png) | Manager phải nhận PENDING thành INVESTIGATING trước khi kết luận. |

## Sitemap diagrams

| ID | Title / purpose | Refs | Editable source | Exports | Giải thích ngắn |
|---|---|---|---|---|---|
| SITE-01 | Public Website Sitemap | VIEW, DISC, AUTH, OTP | [PUML](diagrams/07-sitemap/SITE-01-public.puml) | [SVG](diagrams/exports/SITE-01.svg) · [PNG](diagrams/exports/SITE-01.png) | Route công khai và JSP/redirect đích. |
| SITE-02 | Customer Workspace Sitemap | VIEW, BOOK, MEDIA, MSG | [PUML](diagrams/07-sitemap/SITE-02-customer.puml) | [SVG](diagrams/exports/SITE-02.svg) · [PNG](diagrams/exports/SITE-02.png) | Điều hướng từ dashboard tới booking/deposit/chat/delivery/review. |
| SITE-03 | Photographer Studio Sitemap | VIEW, BOOK, MEDIA | [PUML](diagrams/07-sitemap/SITE-03-photographer.puml) | [SVG](diagrams/exports/SITE-03.svg) · [PNG](diagrams/exports/SITE-03.png) | Onboarding và các màn hình studio thực tế. |
| SITE-04 | Admin Console Sitemap | VIEW, ADMIN | [PUML](diagrams/07-sitemap/SITE-04-admin.puml) | [SVG](diagrams/exports/SITE-04.svg) · [PNG](diagrams/exports/SITE-04.png) | Console hiện có users/photographers/bookings/reviews-reports. |
| SITE-05 | Manager Workspace Sitemap | VIEW, MANAGER | [PUML](diagrams/07-sitemap/SITE-05-manager.puml) | [SVG](diagrams/exports/SITE-05.svg) · [PNG](diagrams/exports/SITE-05.png) | Dedicated routes/layout, paginated queues, không cấp /admin access. |

## Quality-control findings

### Implementation gaps represented in the diagrams

- **No real payment gateway:** deposit is a deterministic/demo workflow using `DemoPaymentMethod`; every affected diagram says “mô phỏng.”
- **External systems unverified:** source integrations exist for Google/Supabase, SMTP and Cloudinary, but this documentation run used no live credentials and makes no production-readiness claim.
- **Legacy delivery exposure:** new delivery uploads request Cloudinary `type=authenticated` (`CloudinaryStorageServiceImpl`, around lines 72–76) and access URLs are signed for 300 seconds (`DeliveryServiceImpl`, around lines 323–359). Already-public legacy assets remain public until explicitly migrated.
- **No chat attachments, category administration, or delivery revision-history entity.** These are not drawn as implemented functions.
- `PhotographerVerificationStatus.SUSPENDED` still lacks a normal transition action; `ReportStatus.INVESTIGATING` is now used by Manager assignment.
- `BookingStatus.DEPOSIT_PENDING` is present and the state machine allows `ACCEPTED -> DEPOSIT_PENDING`, but no production caller performs that transition; normal demo checkout goes from `ACCEPTED` directly to `CONFIRMED` after the deposit becomes `PAID`.

### Entity / migration mismatches and database risk

- `PhotographerProfile.reviewedByAdminId` and `UserReport.resolvedByAdminId` are scalar IDs, not JPA associations; their migrations do not establish foreign keys. ERDs intentionally omit relationship lines.
- `CalendarEvent.userId` is scalar in JPA while its migration provides the database foreign key; ERDs label that distinction.
- **Fresh SQL Server installation blocker:** [`V013__create_saved_photographers.sql`](../src/main/resources/db/migration/V013__create_saved_photographers.sql) declares two cascade routes. A disposable SQL Server 2022 run reproduced error 1785 at V013 before V027 could execute.

### Booking transition review

- [`BookingStateMachineServiceImpl`](../src/main/java/com/photoconnect/service/BookingStateMachineServiceImpl.java) defines the transitions shown in STATE-01 (lines 29–89) and rejects invalid edges.
- Unusual but source-supported paths remain: `ACCEPTED -> COMPLETED`, plus direct completion/delivery transitions from pre-delivery production states. They are labelled legacy/direct in the model instead of being silently normalized.
- `ACCEPTED -> DEPOSIT_PENDING` is an allowed but currently uncalled edge; the executed deposit path accepts either `ACCEPTED` or `DEPOSIT_PENDING` and normally confirms directly from `ACCEPTED`.
- Transition to `CONFIRMED` requires a `PAID` deposit invariant; same-state retries are idempotent and do not append duplicate history.

## Validation and deliverable counts

| Artifact | Count | Result |
|---|---:|---|
| Editable diagram sources | 52 | PlantUML syntax validated |
| SVG exports | 52 | Rendered successfully; preferred for the academic report |
| PNG exports | 52 | Rendered successfully; suitable for Word |
| Feature inventory | 1 | Source-backed status catalogue |
| Shared theme | 1 | Consistent typography/colors |

Representative visual inspection originally covered UC-00, ARCH-01, ERD-01 and STATE-01. Manager additions were syntax checked and rendered; ERD-01 remains intentionally wide and should be embedded as SVG or on a landscape page.
