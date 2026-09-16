# TASK-029 — Professional Demo Payment & Receipt Experience

## Objective

Replace the TASK-015 developer-facing payment action with a polished, explicitly simulated PhotoConnect checkout. This implementation does not connect to a gateway, bank, merchant account, external payment API, or real-money transfer system.

**Human Verification: PARTIAL (Core Flow PASS; Edge Cases Automated/Pending)**

## Architecture

- Spring MVC controller routes render JSP/JSTL checkout, result, and receipt pages.
- `DepositServiceImpl` remains the authority for ownership, booking state, the 30% amount, lifecycle transitions, outcomes, and references.
- A single `Deposit` remains uniquely associated with one `Booking`; no booking system or relationship was rebuilt.
- `DemoQrCodeService` uses ZXing Core locally and emits a self-contained SVG data URI. There is no remote QR/image request.
- `DemoPaymentRequest` is an ephemeral MVC input object. It has no amount, customer, status, or transaction-reference property.
- The repository obtains a pessimistic write lock while completing/cancelling a deposit so repeated or concurrent POSTs cannot create a second successful state change.

## Routes

| Method | Route | Behavior |
|---|---|---|
| GET | `/bookings/{id}/deposit` | TASK-015 compatibility redirect to canonical checkout |
| GET | `/bookings/{id}/deposit/checkout` | Owner-only professional demo checkout; creates/resumes deposit |
| POST | `/bookings/{id}/deposit/process` | Server-authoritative demo QR/card processing |
| POST | `/bookings/{id}/deposit/cancel` | Cancels a pending unpaid attempt |
| GET | `/bookings/{id}/deposit/result` | Professional success/failed/cancelled/current-state result |
| GET | `/bookings/{id}/deposit/receipt` | Owner-only printable receipt for `PAID` deposits |

The old `/deposit/simulate-payment` controller endpoint and prominent developer hook were removed. The TASK-015 service method remains as a deprecated compatibility method and delegates to the secured demo QR path.

## State flow

```text
PENDING -> PROCESSING -> PAID
                      -> FAILED
PENDING -> CANCELLED
```

Existing persisted values remain valid. `REFUNDED` and `FORFEITED` were not renamed or removed. Reopening checkout after `FAILED`/`CANCELLED` begins a fresh pending attempt. A `PAID` deposit is immutable and repeated POSTs return its existing state/reference without saving again.

## Payment methods

### Demo QR / Bank Transfer

The server locally encodes:

```text
PHOTOCONNECT-DEMO|BOOKING:<id>|DEPOSIT:<amount>|REF:<transactionRef>
```

The payload contains no real account, payment URL, credential, or secret. The checkout visibly labels the image `DEMO` and states that no transfer occurs. Confirming the QR option deterministically produces the demo success outcome after the server revalidates owner, role, booking, deposit, and status.

### Demo Card

Fictional deterministic test inputs:

| Number | Outcome |
|---|---|
| `4242 4242 4242 4242` | Success |
| `4000 0000 0000 0002` | Declined |
| Any other/invalid value | Safe generic failure |

Use a non-expired `MM/YY`, a 3–4 digit CVV, and any non-blank demo cardholder name. Cardholder name, number, expiry, and CVV exist only in the request object for the duration of processing. They are never stored in `Deposit`, written to logs, sent externally, or repeated in a failure message.

## Transaction reference

The server generates `PC-yyyyMMdd-XXXXXXXX` using the server date and eight uppercase hexadecimal UUID characters. Browser-supplied `amount` or `transactionReference` parameters have no matching request properties and cannot become authoritative.

## Security rules

- Every route requires a session `CUSTOMER` before service access.
- The service loads `Booking` and compares `Booking.customer.id` with the authenticated session user ID.
- The booking customer entity must itself have role `CUSTOMER`.
- Unpaid processing is allowed only while `Booking.status == ACCEPTED`.
- Deposit amount is calculated once as `Booking.agreedPrice × 0.30`, rounded to two decimals, and never accepted from the form.
- The deposit is loaded under a pessimistic write lock for completion/cancellation.
- `PAID` is idempotent and cannot be downgraded, duplicated, or have its amount/reference replaced by repeat POSTs.
- Receipt and result data are loaded through the same owner-only booking/deposit services.
- Photographer and admin sessions are rejected by the customer route guard.
- Missing resources and invalid/forbidden states use the existing centralized exception system.

## Database changes

Forward-only SQL Server migration:

`docs/development/migrations/V010__professional_demo_payment.sql`

It safely adds nullable `payment_method VARCHAR(30)` and `failure_reason NVARCHAR(255)` columns only when absent. Existing `payment_reference` and `paid_at` columns are reused. No historical migration was edited; there is no drop, truncate, destructive rewrite, or mandatory backfill.

Manual application in SSMS:

1. Select the `PhotoConnect` database.
2. Open and execute `docs/development/migrations/V010__professional_demo_payment.sql`.
3. Confirm `dbo.deposits.payment_method` and `dbo.deposits.failure_reason` exist and old rows remain unchanged.

With the repository's current `spring.jpa.hibernate.ddl-auto=update`, Hibernate can add the same nullable columns in a local development database; the migration is the authoritative manual SQL path.

## Automated verification

Focused TASK-029 verification:

```text
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Coverage includes role gates, cross-customer ownership denial, non-customer rejection, exact 30% calculation, ignored browser amount/reference, local QR payload/data URI, successful/failed/invalid card behavior, cancellation, transient processing persistence, server reference generation, idempotent already-paid handling, invalid booking state, missing booking, receipt ownership/model values, absent sensitive fields on `Deposit`, UI disclosures, and print CSS.

Full verification:

```text
mvn test: Tests run: 396, Failures: 0, Errors: 0, Skipped: 23 — BUILD SUCCESS
mvn clean package: Tests run: 396, Failures: 0, Errors: 0, Skipped: 23 — BUILD SUCCESS
Artifact: target/photoconnect.war
```

## Manual verification

### Manually Verified (PASS)
- Photographer accepted a PENDING booking.
- Customer booking changed to ACCEPTED.
- ACCEPTED booking displayed the 30% deposit requirement.
- "Pay Deposit" opened the new professional checkout.
- Demo checkout page rendered successfully.
- Demo QR payment option rendered successfully.
- Booking #2 displayed correctly in checkout.
- Agreed price displayed as 10,000,000 VND.
- Deposit displayed as 3,000,000 VND.
- Deposit percentage displayed as 30%.
- Remaining balance displayed as 7,000,000 VND.
- Server-generated PC-* demo transaction reference was visible.
- "Demo Payment Environment" / no-real-money disclosure was visible.
- Existing legacy PAID deposit remained compatible.
- Legacy payment receipt rendered successfully.
- UI consistency pass (harmonized button styling across JSPs) visually checked and completed.

### Automated Tests (PASS) / Manual Verification Pending
The following scenarios are covered by automated tests (`DepositControllerTest`, `DepositServiceTest`, `DemoPaymentUiContractTest`, etc.) and remain pending for manual multi-browser verification:
- Failed card scenario (`4000 0000 0000 0002` decline flow)
- Cancelled payment scenario (`/deposit/cancel` returning to booking)
- Duplicate POST / concurrent request pessimistic locking
- Cross-customer IDOR (accessing other customers' checkout/receipt)
- Photographer/admin checkout access denial
- DevTools amount/reference tampering rejection
- Full mobile responsive matrix testing across devices

