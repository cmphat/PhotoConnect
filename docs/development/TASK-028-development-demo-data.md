# TASK-028: Development Demo Data

## Purpose

Complete the Week 7 / Phase 9 seed data requirement from `docs/ROADMAP.md` and `docs/PLAN_BE.md`. Provides a safe, idempotent, non-destructive dataset suitable for marketplace pagination verification (at least 13 approved photographers), end-to-end local testing, and multi-role platform demonstrations.

## Scope

- Provide at least 13 `APPROVED` photographer profiles (implemented with 14 approved profiles) to verify two full pages in the public marketplace (Page 1 with 12 cards, Page 2 with 2 cards).
- Distribute photographers across three major Vietnamese locations: Hanoi (5), Ho Chi Minh City (5), and Da Nang (4).
- Provide diverse starting rates from 500,000 VND to 4,500,000 VND and experience from 1 to 12 years.
- Include 1 `PENDING` photographer application (`photographer15@photoconnect.test`) to demonstrate admin approval/rejection workflows (`/admin/photographers`).
- Create 1 `ADMIN` account and 2 `CUSTOMER` accounts.
- Seed sample portfolio images with high-resolution editorial photography URLs.
- Seed end-to-end booking records across `COMPLETED`, `ACCEPTED`, and `PENDING` states.
- Seed deposit records (`PAID` with reference and `PENDING`).
- Seed customer review with rating (5 stars) and pre-calculated profile metrics.
- Seed real-time chat history between customer and photographer.
- Seed a photographer unavailable date to test schedule blocking.

## Safety & Idempotency Rules

1. **Strictly Non-Destructive**: No `DROP TABLE`, `TRUNCATE`, or blind deletes. Existing database records are preserved.
2. **Fully Idempotent**: All inserts check for existing emails/records before writing. The seeding can be executed multiple times safely.
3. **Development-Only Credentials**: All seeded accounts use the development password `password123` or `PhotoConnectDemo!2026` hashed with BCrypt. No real credentials or production secrets are stored.
4. **Zero Production Impact**: Seeding is strictly opt-in and disabled by default.

---

## Seed variants and intended records

The two mechanisms are intentionally different and must not be treated as equivalent:

- `V009__demo_seed_data.sql` is the **full workflow dataset** described below: 1 admin, 2 customers, 14 approved photographers, 1 pending photographer, plus guarded portfolio, availability, booking, deposit, review, and chat records.
- `DemoDataSeeder` is a **smaller application-startup dataset**: 1 admin, 1 customer, and 15 approved photographer accounts/profiles. It does not create bookings, deposits, reviews, messages, portfolio images, unavailable dates, or a pending application.

Both are opt-in, idempotent, and non-destructive. Matching existing records are left unchanged. These are intended inserts into an empty compatible database, not guaranteed total row counts after seeding a database that already contains development data.

## Full SQL seed dataset specification

### Accounts & Credentials

| Role | Email | Password | Full Name | Phone | Notes |
|---|---|---|---|---|---|
| **ADMIN** | `admin@photoconnect.test` | `password123` | System Administrator | `0901000001` | Full administrative access (`/admin/dashboard`) |
| **CUSTOMER** | `customer1@photoconnect.test` | `password123` | Nguyen Van An | `0902000001` | Has completed booking, paid deposit, review, and chat |
| **CUSTOMER** | `customer2@photoconnect.test` | `password123` | Le Thi Binh | `0902000002` | Has accepted booking with pending deposit |
| **PHOTOGRAPHER** | `photographer1@photoconnect.test` | `password123` | Tran Minh Quang | `0903000001` | Approved, Minh Quang Editorial (Hanoi), 8 yrs exp, 2.5M VND |
| **PHOTOGRAPHER** | `photographer2@photoconnect.test` | `password123` | Vu Hoang Nam | `0903000002` | Approved, Nam Vu Fine Art (Hanoi), 5 yrs exp, 1.8M VND |
| **PHOTOGRAPHER** | `photographer3@photoconnect.test` | `password123` | Doan Thu Trang | `0903000003` | Approved, Thu Trang Studio (Hanoi), 3 yrs exp, 1.2M VND |
| **PHOTOGRAPHER** | `photographer4@photoconnect.test` | `password123` | Pham Quoc Bao | `0903000004` | Approved, Bao Pham Architecture (Hanoi), 10 yrs exp, 3.5M VND |
| **PHOTOGRAPHER** | `photographer5@photoconnect.test` | `password123` | Bui Thanh Tung | `0903000005` | Approved, Tung Bui Moments (Hanoi), 2 yrs exp, 800k VND |
| **PHOTOGRAPHER** | `photographer6@photoconnect.test` | `password123` | Nguyen Hoang Long | `0903000006` | Approved, Long Nguyen Visuals (HCMC), 12 yrs exp, 4.5M VND |
| **PHOTOGRAPHER** | `photographer7@photoconnect.test` | `password123` | Tran Thi Mai | `0903000007` | Approved, Mai Tran Portraits (HCMC), 6 yrs exp, 2.0M VND |
| **PHOTOGRAPHER** | `photographer8@photoconnect.test` | `password123` | Dinh Van Phuc | `0903000008` | Approved, Phuc Dinh Culinary (HCMC), 4 yrs exp, 1.5M VND |
| **PHOTOGRAPHER** | `photographer9@photoconnect.test` | `password123` | Hoang Duc Thang | `0903000009` | Approved, Thang Hoang Street (HCMC), 7 yrs exp, 2.2M VND |
| **PHOTOGRAPHER** | `photographer10@photoconnect.test` | `password123` | Ngo Bao Chau | `0903000010` | Approved, Chau Ngo Studio (HCMC), 1 yr exp, 500k VND |
| **PHOTOGRAPHER** | `photographer11@photoconnect.test` | `password123` | Cao Xuan Bach | `0903000011` | Approved, Bach Cao Coastal (Da Nang), 9 yrs exp, 3.0M VND |
| **PHOTOGRAPHER** | `photographer12@photoconnect.test` | `password123` | Phan My Linh | `0903000012` | Approved, My Linh Lifestyle (Da Nang), 4 yrs exp, 1.4M VND |
| **PHOTOGRAPHER** | `photographer13@photoconnect.test` | `password123` | Luu Tuan Anh | `0903000013` | Approved, Tuan Anh Drone (Da Nang), 6 yrs exp, 2.8M VND |
| **PHOTOGRAPHER** | `photographer14@photoconnect.test` | `password123` | Trinh Thu Hang | `0903000014` | Approved, Hang Trinh Vintage (Da Nang), 3 yrs exp, 1.0M VND |
| **PHOTOGRAPHER** | `photographer15@photoconnect.test` | `password123` | Dang Gia Huy | `0903000015` | **PENDING**, Gia Huy Experimental (Hanoi), 2 yrs exp, 900k VND |

---

## How to Activate / Execute the Seed

Two safe but intentionally different execution methods are provided:

### Method 1: Direct SQL Script via SSMS or `sqlcmd` (Recommended for Database Admin)

The script `docs/development/seed/V009__demo_seed_data.sql` is fully idempotent and can be executed directly against SQL Server:

#### Option A: SQL Server Management Studio (SSMS)
1. Open SSMS and connect to your local SQL Server instance.
2. Open `docs/development/seed/V009__demo_seed_data.sql`.
3. Select `PhotoConnect` database.
4. Execute (`F5`).

#### Option B: PowerShell with `sqlcmd`
```powershell
sqlcmd -S localhost -d PhotoConnect -U sa -P $env:DB_PASSWORD -i docs/development/seed/V009__demo_seed_data.sql
```
Or using Windows Authentication:
```powershell
sqlcmd -S localhost -E -d PhotoConnect -i docs/development/seed/V009__demo_seed_data.sql
```

### Method 2: Spring Boot Application Runner (accounts/profiles only)

The `DemoDataSeeder` component is bundled in the application but dormant unless both the profile and flag are supplied:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=demo-seed" "-Dspring-boot.run.arguments=--photoconnect.demo.seed-enabled=true"
```

To specify a custom password during seed:
```powershell
$env:DEMO_PASSWORD="YourPassword123!"
mvn spring-boot:run "-Dspring-boot.run.profiles=demo-seed" "-Dspring-boot.run.arguments=--photoconnect.demo.seed-enabled=true"
```

---

## Verification Checklist

1. **Marketplace Pagination (TASK-025)**:
   - Navigate to `http://localhost:8080/photographers`.
   - Page 1 shows 12 approved photographer cards.
   - Click **Next**; Page 2 shows 2 approved photographer cards (`photographer13`, `photographer14`).
   - Filter by City "Hanoi" -> 5 results; Filter by City "Ho Chi Minh City" -> 5 results; "Da Nang" -> 4 results.
   - Filter by Min Price 2,000,000 VND -> results dynamically update across pagination.
2. **Admin Approval (TASK-009 / TASK-020)**:
   - Log in as `admin@photoconnect.test` / `password123`.
   - Visit `/admin/photographers?status=PENDING` -> `Gia Huy Experimental` is listed for review and approval.
3. **Booking & Review Flow**:
   - Log in as `customer1@photoconnect.test` / `password123`.
   - Visit `/bookings` -> view the completed photoshoot booking and client review.
4. **Photographer Dashboard**:
   - Log in as `photographer1@photoconnect.test` / `password123`.
   - Visit `/photographer/portfolio` -> view portfolio images.
   - Visit `/photographer/schedule` -> view blocked off-platform date.

## Automated Verification

- `DemoDataSeederTest` verifies the application runner's intended 17-account / 15-profile creation calls, idempotence, existing-account preservation, and password bounds. Those counts describe an empty database run, not guaranteed total database rows.
- Full regression suite (`mvn test`) runs cleanly with 0 failures and 0 errors.

## Human Verification

**PASS (manually verified by project owner):** Demo seed data was successfully executed and loaded into the live local SQL Server database; the seeded photographer records visibly appeared and were verified in the live application.

## Status

Implementation complete. Automated tests PASS. Human Verification: PASS (live local SQL Server seed execution and seeded photographer visibility verified in runtime application).
