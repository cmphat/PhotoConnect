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
- `[ ]` inspect current booking flow
- `[ ]` design Deposit entity
- `[ ]` create DepositStatus enum
- `[ ]` create Deposit entity
- `[ ]` create DepositRepository
- `[ ]` create DepositService
- `[ ]` calculate deposit server-side
- `[ ]` prevent duplicate deposit
- `[ ]` prevent deposit before ACCEPTED
- `[ ]` create customer deposit page
- `[ ]` create development simulated payment action
- `[ ]` protect ownership
- `[ ]` add tests
- `[ ]` integration tests
- `[ ]` regression tests
- `[ ]` build
- `[ ]` runtime verify
- `[ ]` documentation
- `[ ]` human verification
