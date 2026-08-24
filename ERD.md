# ERD.md — Thiết Kế Cơ Sở Dữ Liệu

> DBMS: SQL Server  
> ORM: Spring Data JPA / Hibernate

---

## 1. Quan hệ tổng quát

```text
users
├── 0..1 photographer_profiles
├── 1..* bookings (customer)
├── 1..* messages (sender/receiver)
└── 1..* reviews (customer)

photographer_profiles
├── 1..* portfolio_items
├── 1..* service_packages
├── 1..* photographer_categories
├── 1..* bookings
└── 1..* reviews

bookings
├── 1 service_package
├── 1 customer
├── 1 photographer
├── 0..* messages
└── 0..1 review
```

---

## 2. Bảng `users`

| Column | Type | Note |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| full_name | NVARCHAR(120) | not null |
| email | NVARCHAR(150) | unique |
| password_hash | NVARCHAR(255) | BCrypt |
| phone | NVARCHAR(20) | nullable |
| avatar_url | NVARCHAR(500) | nullable |
| role | VARCHAR(30) | CUSTOMER / PHOTOGRAPHER / ADMIN |
| status | VARCHAR(20) | ACTIVE / LOCKED |
| created_at | DATETIME2 | |
| updated_at | DATETIME2 | |

Index:
- unique `email`

---

## 3. `photographer_profiles`

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| user_id | BIGINT FK users(id), unique |
| bio | NVARCHAR(MAX) |
| location | NVARCHAR(255) |
| experience_years | INT |
| price_from | DECIMAL(18,2) |
| approval_status | VARCHAR(20) |
| average_rating | DECIMAL(3,2) |
| review_count | INT |
| created_at | DATETIME2 |
| updated_at | DATETIME2 |

---

## 4. `categories`

Ví dụ: Wedding, Portrait, Event, Product, Graduation.

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| name | NVARCHAR(100) UNIQUE |
| slug | VARCHAR(120) UNIQUE |
| status | VARCHAR(20) |

---

## 5. `photographer_categories`

Many-to-many.

| Column | Type |
|---|---|
| photographer_id | BIGINT FK |
| category_id | BIGINT FK |

Primary key:
`(photographer_id, category_id)`

---

## 6. `portfolio_items`

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| photographer_id | BIGINT FK |
| image_url | NVARCHAR(700) |
| cloudinary_public_id | NVARCHAR(300) |
| title | NVARCHAR(150) |
| description | NVARCHAR(500) |
| category_id | BIGINT FK nullable |
| status | VARCHAR(20) |
| created_at | DATETIME2 |

Không lưu binary ảnh trong SQL Server.

---

## 7. `service_packages`

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| photographer_id | BIGINT FK |
| name | NVARCHAR(150) |
| description | NVARCHAR(MAX) |
| price | DECIMAL(18,2) |
| duration_minutes | INT |
| max_photos | INT nullable |
| status | VARCHAR(20) |
| created_at | DATETIME2 |
| updated_at | DATETIME2 |

---

## 8. `bookings`

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| customer_id | BIGINT FK users(id) |
| photographer_id | BIGINT FK photographer_profiles(id) |
| service_package_id | BIGINT FK service_packages(id) |
| start_time | DATETIME2 |
| end_time | DATETIME2 |
| location | NVARCHAR(500) |
| note | NVARCHAR(MAX) |
| total_price | DECIMAL(18,2) |
| status | VARCHAR(30) |
| created_at | DATETIME2 |
| updated_at | DATETIME2 |

Index khuyến nghị:
- `(photographer_id, start_time, end_time)`
- `(customer_id, created_at)`
- `(status)`

---

## 9. `booking_status_history`

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| booking_id | BIGINT FK |
| old_status | VARCHAR(30) |
| new_status | VARCHAR(30) |
| changed_by_user_id | BIGINT FK users(id) |
| note | NVARCHAR(500) |
| created_at | DATETIME2 |

Dùng để audit luồng booking.

---

## 10. `messages`

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| booking_id | BIGINT FK |
| sender_id | BIGINT FK users(id) |
| receiver_id | BIGINT FK users(id) |
| content | NVARCHAR(2000) |
| is_read | BIT |
| sent_at | DATETIME2 |

Index:
- `(booking_id, sent_at)`

---

## 11. `reviews`

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| booking_id | BIGINT FK UNIQUE |
| customer_id | BIGINT FK users(id) |
| photographer_id | BIGINT FK photographer_profiles(id) |
| rating | TINYINT |
| comment | NVARCHAR(2000) |
| status | VARCHAR(20) |
| created_at | DATETIME2 |

Constraint logic:
- rating 1..5
- booking unique

---

## 12. `notifications` — optional nhưng nên có

| Column | Type |
|---|---|
| id | BIGINT IDENTITY PK |
| user_id | BIGINT FK |
| type | VARCHAR(50) |
| title | NVARCHAR(200) |
| content | NVARCHAR(1000) |
| reference_id | BIGINT nullable |
| is_read | BIT |
| created_at | DATETIME2 |

---

## 13. Bonus tables

Chỉ tạo khi core đã xong:

### vouchers
### voucher_usages
### deposits

---

## 14. Thứ tự tạo entity

```text
1. User
2. PhotographerProfile
3. Category
4. PhotographerCategory
5. PortfolioItem
6. ServicePackage
7. Booking
8. BookingStatusHistory
9. Message
10. Review
11. Notification
```
