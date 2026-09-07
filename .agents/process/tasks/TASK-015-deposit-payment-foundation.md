# TASK-015: Deposit and Payment Foundation

## 1. Goal
Introduce a payment/deposit FOUNDATION for accepted bookings. 
This task will NOT integrate a real payment gateway. We are building the data model, state machine, service logic, and a simulated development-only payment flow for a 30% deposit requirement on ACCEPTED bookings.

## 2. Business Rules
- **Deposit Creation**: A deposit record is only created when a Booking is `ACCEPTED`.
- **Deposit Amount**: 30% of the `Booking.agreedPrice` (calculated server-side).
- **Simulated Payment**: Development-only POST route to transition a deposit to `PAID` with a fake reference.
- **Idempotency**: One booking has exactly one deposit. Do not create duplicates.

## 3. Entity Design
### `DepositStatus` Enum
- PENDING
- PAID
- FAILED
- REFUNDED
- FORFEITED

### `Deposit` Entity (Table: `deposits`)
- `id` (Long, PK)
- `booking` (@OneToOne, nullable = false, unique = true)
- `amount` (BigDecimal, precision = 18, scale = 2)
- `status` (DepositStatus, EnumType.STRING)
- `paymentReference` (String)
- `paidAt` (LocalDateTime)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

## 4. State Machine
- Creation: `PENDING`
- Successful Payment (Simulated): `PENDING` -> `PAID`
- Note: Booking status remains `ACCEPTED` even when deposit is `PAID` for now.

## 5. Authorization
- Only the authenticated Customer who owns the Booking can view or pay the deposit.

## 6. Implementation Checklist
- `[x]` inspect current booking flow
- `[x]` design Deposit entity
- `[x]` create DepositStatus enum
- `[x]` create Deposit entity
- `[x]` create DepositRepository
- `[x]` create DepositService
- `[x]` calculate deposit server-side
- `[x]` prevent duplicate deposit
- `[x]` prevent deposit before ACCEPTED
- `[x]` create customer deposit page
- `[x]` create development simulated payment action
- `[x]` protect ownership
- `[x]` add tests
- `[x]` integration tests
- `[x]` regression tests
- `[x]` build
- `[x]` runtime verify
- `[x]` documentation
- `[x]` human verification (Status: PASS)

## 7. Human Verification Results (Status: PASS)
- CUSTOMER creates booking (starts as PENDING).
- PHOTOGRAPHER accepts booking (transitions to ACCEPTED).
- CUSTOMER accesses `/bookings/{id}/deposit`.
- 30% calculation verified: `agreedPrice = 10,000,000 VND` -> `deposit = 3,000,000 VND`.
- Simulated development payment succeeds.
- Deposit status becomes `PAID`.
- DEV reference code generated and displayed.
