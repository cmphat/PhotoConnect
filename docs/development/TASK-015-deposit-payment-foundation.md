# TASK-015: Deposit and Payment Foundation

## Overview
This document explains the implementation of the deposit and payment foundation in PhotoConnect, satisfying TASK-015 requirements. It introduces the data structures, state machine, and a simulated development payment flow for handling 30% booking deposits.

## Why Deposits Exist
In a professional photography marketplace, no-shows or last-minute cancellations result in lost income and time for photographers. A deposit guarantees commitment from the customer and secures the booking slot.

## Booking vs Deposit Separation
The `Booking` entity models the event itself (who, when, where).
The `Deposit` entity models the financial transaction securing that event.
This separation of concerns ensures that the booking lifecycle (PENDING -> ACCEPTED -> COMPLETED) is distinct from the payment lifecycle (PENDING -> PAID). For example, a booking can be ACCEPTED before the deposit is PAID.

## The OneToOne Relationship
A `Deposit` is mapped with a `@OneToOne` relationship to a `Booking`. The deposit table has a `booking_id` foreign key that is `unique = true`, ensuring that one booking has at most one deposit record. This enforces idempotency at the database level.

## Money Handling: BigDecimal
All monetary values (`Booking.agreedPrice` and `Deposit.amount`) are stored using `BigDecimal` with precision 18 and scale 2. This guarantees deterministic calculations and prevents floating-point precision errors that occur with `float` or `double`.

## Server-Controlled Calculations
Deposit amounts are never trusted from the client/browser. When a deposit is created, the server retrieves the `Booking.agreedPrice` from the database and calculates the `amount` by multiplying it by a constant rate (`0.30`).

## Deposit Status State Machine
- `PENDING`: The deposit record has been created, awaiting payment.
- `PAID`: The deposit has been successfully paid.
- `FAILED` / `REFUNDED` / `FORFEITED`: Added to the enum for future business rules (e.g. no-shows or cancellations), but not used in the happy path of TASK-015.

## Idempotency
- If a customer tries to access the deposit page multiple times, `DepositService.getOrCreateDepositForBooking` checks if a deposit already exists. If so, it returns the existing deposit instead of creating duplicates.
- The `simulateSuccessfulPayment` method checks if the deposit is already `PAID` and returns immediately without error if it is, providing an idempotent response.

## Simulated Payment vs Real Gateway
This task implements a simulated payment flow for development purposes. It updates the deposit status to `PAID` and generates a fake `DEV-...` payment reference. It DOES NOT integrate with a real payment gateway (like Stripe, VNPay, etc.) and does not collect real credit card information. A real gateway integration will be handled in a future task.

## Environment Switch
The simulation is protected by an environment property:
`photoconnect.payment.simulation-enabled=${PAYMENT_SIMULATION_ENABLED:true}`
If disabled in production, the `/bookings/{id}/deposit/simulate-payment` endpoint will throw an `IllegalStateException`.

## Authorization
Deposit endpoints verify that the authenticated `userId` matches the `Booking.customer.id`. Customers cannot view or pay deposits for bookings they do not own.

## Future Scope (Escrow/Refund/No-show)
The foundation is prepared for future rules:
- If a customer no-shows, the deposit may transition to `FORFEITED`.
- If a photographer no-shows, the deposit may transition to `REFUNDED`, and they may face a penalty.
- A real payment gateway will handle the actual capture of funds.

## Controller -> Service -> Repository -> SQL Flow
1. **Controller**: Validates session, maps DTOs, and calls Service layer.
2. **Service**: Enforces business rules (must be ACCEPTED, must be owner), performs calculations, and calls Repository.
3. **Repository**: Executes SQL queries, including `JOIN FETCH` to prevent N+1 queries.
4. **SQL**: Persists the `deposits` table row.

## Tests
- `DepositServiceTest`: Validates 30% calculation, status transitions, idempotency, and authorization rules using Mockito.
- `DepositRepositoryIntegrationTests`: Validates database constraints and the `@OneToOne` mapping using an H2/SQLServer integration test.
- `DepositControllerTest`: Verifies web layer routing and authorization using `MockMvc`.
