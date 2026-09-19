# PhotoConnect Database Model

> Database: Microsoft SQL Server
>
> Mapping: Spring Data JPA / Hibernate
> Source of truth: entity mappings under `src/main/java/com/photoconnect/entity`

## Relationships

```text
User 1 -------- 0..1 PhotographerProfile
User 1 -------- 0..* Booking (as customer)
User 1 -------- 0..* Message (as sender)
User 1 -------- 0..* Message (as receiver)
User 1 -------- 0..* Review (as customer)

PhotographerProfile 1 -------- 0..* PortfolioImage
PhotographerProfile 1 -------- 0..* PhotographerUnavailableDate
PhotographerProfile 1 -------- 0..* Booking
PhotographerProfile 1 -------- 0..* Review

Booking 1 -------- 0..1 Deposit
Booking 1 -------- 0..1 Review
Booking 1 -------- 0..* Message
```

There are no implemented category, service-package, booking-history, notification, or voucher entities/tables in the final scope.

## `users` — `User`

| Column | Mapping | Rules |
|---|---|---|
| `id` | `BIGINT IDENTITY` | primary key |
| `email` | string(255) | non-null, unique |
| `password` | string(255) | non-null BCrypt hash |
| `full_name` | string(150) | non-null |
| `phone` | string(20) | nullable |
| `role` | string enum | `CUSTOMER`, `PHOTOGRAPHER`, `ADMIN` |
| `status` | string enum | `ACTIVE`, `INACTIVE`, `BANNED` |
| `created_at` | datetime | creation timestamp |
| `updated_at` | datetime | update timestamp |

## `photographer_profiles` — `PhotographerProfile`

| Column | Mapping | Rules |
|---|---|---|
| `id` | `BIGINT IDENTITY` | primary key |
| `user_id` | FK -> `users.id` | non-null, unique |
| `display_name` | string(150) | non-null |
| `bio` | `NVARCHAR(MAX)` | nullable |
| `city` | string(100) | nullable |
| `experience_years` | integer | nullable |
| `price_from` | decimal(18,2) | nullable |
| `verification_status` | string enum | `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED` |
| `average_rating` | double | cached visible-review average |
| `review_count` | integer | non-null cached visible-review count |
| `created_at` | datetime | creation timestamp |
| `updated_at` | datetime | update timestamp |

## `portfolio_images` — `PortfolioImage`

| Column | Mapping | Rules |
|---|---|---|
| `id` | `BIGINT IDENTITY` | primary key |
| `photographer_profile_id` | FK -> `photographer_profiles.id` | non-null |
| `image_url` | string(2048) | non-null Cloudinary HTTPS URL |
| `public_id` | string(512) | non-null Cloudinary asset ID |
| `caption` | string(500) | nullable |
| `display_order` | integer | default 0 |
| `created_at` | datetime | creation timestamp |

Image binaries are not stored in SQL Server.

## `bookings` — `Booking`

| Column | Mapping | Rules |
|---|---|---|
| `id` | `BIGINT IDENTITY` | primary key |
| `customer_id` | FK -> `users.id` | non-null |
| `photographer_profile_id` | FK -> `photographer_profiles.id` | non-null |
| `booking_date` | date | non-null |
| `booking_time` | time | non-null |
| `location` | string(255) | non-null |
| `notes` | string(1000) | nullable |
| `agreed_price` | decimal(18,2) | non-null server snapshot |
| `status` | string enum | `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `COMPLETED` |
| `created_at` | datetime | non-null |
| `updated_at` | datetime | nullable/update time |

## `deposits` — `Deposit`

| Column | Mapping | Rules |
|---|---|---|
| `id` | `BIGINT IDENTITY` | primary key |
| `booking_id` | FK -> `bookings.id` | non-null, unique |
| `amount` | decimal(18,2) | non-null; server-calculated 30% snapshot |
| `status` | string enum | `PENDING`, `PROCESSING`, `PAID`, `FAILED`, `CANCELLED`; legacy `REFUNDED`, `FORFEITED` retained |
| `payment_reference` | string | nullable, server generated |
| `payment_method` | string enum | nullable `DEMO_QR` or `DEMO_CARD` |
| `failure_reason` | string(255) | nullable safe demo result |
| `paid_at` | datetime | nullable |
| `created_at` | datetime | non-null |
| `updated_at` | datetime | nullable/update time |

Card number, expiry, CVV, and cardholder input have no entity columns and are never persisted.

## `reviews` — `Review`

| Column | Mapping | Rules |
|---|---|---|
| `id` | `BIGINT IDENTITY` | primary key |
| `booking_id` | FK -> `bookings.id` | non-null, unique |
| `customer_id` | FK -> `users.id` | non-null |
| `photographer_profile_id` | FK -> `photographer_profiles.id` | non-null |
| `rating` | integer | non-null; validated 1–5 |
| `comment` | string(1000) | nullable |
| `status` | string enum | non-null `VISIBLE` or `HIDDEN` |
| `created_at` | datetime | non-null |
| `updated_at` | datetime | nullable |

Only reviews for completed, customer-owned bookings are accepted. Public aggregates use visible reviews.

## `messages` — `Message`

| Column | Mapping | Rules |
|---|---|---|
| `id` | `BIGINT IDENTITY` | primary key |
| `booking_id` | FK -> `bookings.id` | non-null |
| `sender_id` | FK -> `users.id` | non-null booking participant |
| `receiver_id` | FK -> `users.id` | non-null derived participant |
| `content` | string(2000) | non-null |
| `is_read` | bit | non-null, default false |
| `sent_at` | datetime | non-null |

Index: `(booking_id, sent_at)`.

## `photographer_unavailable_dates` — `PhotographerUnavailableDate`

| Column | Mapping | Rules |
|---|---|---|
| `id` | `BIGINT IDENTITY` | primary key |
| `photographer_profile_id` | FK -> `photographer_profiles.id` | non-null |
| `unavailable_date` | date | non-null |
| `reason` | string(255) | nullable |

Unique constraint: `(photographer_profile_id, unavailable_date)`.

## Schema history through V010

- The base development schema was created/evolved by the JPA mappings with `spring.jpa.hibernate.ddl-auto=update`.
- `V008__add_review_status.sql` is a guarded SQL Server migration adding non-null `reviews.status` with default `VISIBLE`.
- `V009__demo_seed_data.sql` is optional, idempotent, non-destructive demo **data**, not a schema migration.
- `V010__professional_demo_payment.sql` is a forward-only transaction adding nullable `deposits.payment_method` and `deposits.failure_reason` when absent.

Historical scripts were audited but not rewritten. In a controlled environment, apply required manual migrations in version order and use `ddl-auto=validate` or a migration runner once schema management is formalized.
