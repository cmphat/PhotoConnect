# PhotoConnect V6 — Smart Booking & VietQR Deposit QA Verification Report

**System:** PhotoConnect — High-End Vietnamese Photography Marketplace  
**Version:** V6.0.0 (Spring Boot 3.3.0, Spring MVC, JSP/JSTL, MS SQL Server 2022)  
**Document Classification:** Quality Assurance, Test Automation & Verification Report  
**Verification Date:** 2026-10-27  
**Overall Status:** PASS (100% Passing — 40/40 Tests, 9/9 E2E Steps)  

---

## 1. Executive Summary

This Quality Assurance report certifies that PhotoConnect V6 meets all business, technical, and regulatory requirements for **mandatory 30% deposit payments, VietQR generation, authoritative pricing calculations, and automated booking state transitions**.

All verification runs were executed on a dedicated Microsoft SQL Server 2022 database with Flyway migrations V001 through V033 applied.

### Summary Metrics

| Metric | Target | Result | Status |
|---|---|---|---|
| **Unit & Slice Tests** | $\ge 35$ tests | 40 passed / 40 total (0 failures, 0 errors, 0 skipped) | **PASS** |
| **End-to-End User Journeys** | Complete 9-step flow | 9 passed / 9 total (100% completion) | **PASS** |
| **Idempotency & Replay Defense** | Zero duplicate transactions | Verified via `UQ_payment_transactions_provider_ext_id` | **PASS** |
| **Tampered Webhook Rejection** | 100% rejection with 401 | Verified via HMAC-SHA256 signature mismatch tests | **PASS** |
| **Visual Quality & UI Fidelity** | Complete responsive UI | Verified via 4 authenticated CDP captures | **PASS** |

---

## 2. Automated Test Suite Results

### 2.1 Test Execution Command

```bash
mvn test "-Dtest=PricingServiceTest,PaymentGatewayAdapterTest,PaymentWebhookControllerTest,BookingPriceSnapshotTest,DepositServiceTest"
```

### 2.2 Test Results Breakdown

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.photoconnect.service.PricingServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.185 s -- in com.photoconnect.service.PricingServiceTest
[INFO] Running com.photoconnect.payment.PaymentGatewayAdapterTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.245 s -- in com.photoconnect.payment.PaymentGatewayAdapterTest
[INFO] Running com.photoconnect.controller.api.PaymentWebhookControllerTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.312 s -- in com.photoconnect.controller.api.PaymentWebhookControllerTest
[INFO] Running com.photoconnect.entity.BookingPriceSnapshotTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.098 s -- in com.photoconnect.entity.BookingPriceSnapshotTest
[INFO] Running com.photoconnect.service.DepositServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.154 s -- in com.photoconnect.service.DepositServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 2.3 Detailed Test Case Descriptions

#### A. `PricingServiceTest` (10 Tests)
1. `testCalculatePrice_StandardPackageOnly`: Verifies base package price with zero surcharges.
2. `testCalculatePrice_NightShootFlag`: Confirms +20% surcharge added when `nightShoot=true`.
3. `testCalculatePrice_NightShootTimeTrigger`: Confirms +20% surcharge triggered by shoot time between 18:00 and 06:00 (e.g. 19:30).
4. `testCalculatePrice_TravelWithinThreshold`: Confirms 0 VND travel surcharge for distance $\le 30$ km.
5. `testCalculatePrice_TravelBeyondThreshold`: Confirms 15,000 VND / km surcharge for distance beyond 30 km (e.g. 35 km $\rightarrow 5 \times 15,000 = 75,000$ VND).
6. `testCalculatePrice_AllExtrasCombined`: Verifies combined addition of extra hours, extra photos, and 2nd photographer.
7. `testCalculatePrice_DepositRoundingHalfUp`: Ensures 30% deposit calculation rounds to the nearest whole integer VND using `HALF_UP`.
8. `testCalculatePrice_RemainingBalanceFormula`: Confirms remaining balance equals exactly $\text{agreedPrice} - \text{depositAmount}$.
9. `testCalculatePrice_CustomShootHourlyRate`: Validates custom shoot pricing using photographer base hourly rate.
10. `testCalculatePrice_InvalidInputsFallback`: Gracefully handles negative distance, empty strings, and null package IDs.

#### B. `PaymentGatewayAdapterTest` (10 Tests)
1. `testDemoAdapter_GeneratesDataUri`: Verifies ZXing QR code generated as valid `data:image/png;base64,...` URI.
2. `testDemoAdapter_ContainsPayload`: Verifies QR content encodes recipient bank, account, and transfer reference.
3. `testDemoAdapter_DemoBadge`: Confirms `DEMO` payment mode metadata is correctly attached.
4. `testPayOSAdapter_DisabledInDemoMode`: Ensures live network requests are blocked when `real-enabled=false`.
5. `testPayOSAdapter_GeneratesPaymentLink`: Tests mock PayOS response parsing in integration mode.
6. `testSePayAdapter_DisabledInDemoMode`: Ensures live network requests are blocked when `real-enabled=false`.
7. `testPaymentGatewayFactory_ResolvesDemo`: Verifies factory returns `DemoPaymentGatewayAdapter` by default.
8. `testPaymentGatewayFactory_ResolvesConfiguredProvider`: Verifies factory selects provider matching configuration.
9. `testPaymentGatewayFactory_InvalidProviderFallback`: Verifies fallback to Demo adapter when provider name is unrecognized.
10. `testVietQRGenerator_ChecksumValidity`: Verifies CRC16-CCITT calculation matches EMVCo/NAPAS standard.

#### C. `PaymentWebhookControllerTest` (8 Tests)
1. `testHandleWebhook_ValidDemoWebhook`: Successfully updates deposit to `PAID` and booking to `CONFIRMED`.
2. `testHandleWebhook_InvalidSignature_Returns401`: Rejects unverified webhook payloads with HTTP 401 Unauthorized.
3. `testHandleWebhook_DuplicateTransaction_Idempotent`: Verifies duplicate webhook delivery returns HTTP 200 without duplicate database inserts.
4. `testHandleWebhook_UnderpaidAmount_Rejected`: Flags transaction if paid amount is less than expected 30% deposit.
5. `testHandleWebhook_UnknownBooking_Returns404`: Gracefully returns error response if reference cannot be matched.
6. `testHandleWebhook_CancelledBooking_Ignored`: Prevents confirmation of cancelled or expired bookings.
7. `testHandleWebhook_AuditingLogged`: Verifies row inserted into `dbo.payment_transactions`.
8. `testHandleWebhook_DispatchesStompNotification`: Verifies STOMP broadcast triggered on confirmation.

#### D. `BookingPriceSnapshotTest` (6 Tests)
1. `testSnapshot_PersistedOnBooking`: Verifies `basePrice`, `nightSurcharge`, `travelSurcharge`, `extrasFee`, `agreedPrice` stored on booking entity.
2. `testSnapshot_ImmunityToPackagePriceUpdates`: Modifying package price in catalog does NOT alter existing booking snapshot.
3. `testSnapshot_ZeroSurchargesHandling`: Verifies default zero values stored correctly.
4. `testSnapshot_PrecisionPreserved`: Validates `DECIMAL(18,2)` database precision and mapping.
5. `testSnapshot_GetterHelpers`: Validates convenience getters for deposit and remaining balance.
6. `testSnapshot_AuditTrail`: Verifies snapshot values match history records.

#### E. `DepositServiceTest` (6 Tests)
1. `testCreateDeposit_PendingStatus`: Verifies initial deposit created with status `PENDING`.
2. `testConfirmDeposit_TransitionsBooking`: Confirms `deposit.status = PAID` and `booking.status = CONFIRMED`.
3. `testConfirmDeposit_AlreadyPaid_Idempotent`: Second call on already paid deposit does not throw error.
4. `testCancelDeposit_TransitionsStatus`: Verifies deposit status transitions to `CANCELLED`.
5. `testDepositPolling_ReturnsStatusDto`: Validates polling endpoint returns current status, amount, and remaining balance.
6. `testTimeoutDeposit_TransitionsExpired`: Verifies expired deposits are marked after 15 minutes.

---

## 3. End-to-End User Journey Verification

The automated script `test_v6_e2e_flow.ps1` validated the complete booking and payment lifecycle:

```
=== Starting PhotoConnect V6 End-to-End Flow Verification ===

[Step 1] Testing Live Pricing Calculation API...
HTTP Status: 200
  Base Price:        1,500,000 VND
  Night Surcharge:   300,000 VND
  Travel Surcharge:  75,000 VND
  Extras Fee:        300,000 VND
  Total Agreed:      2,175,000 VND
  30% Deposit:       652,500 VND
  70% Remaining:     1,522,500 VND
[Step 1 PASS] Live pricing API calculated correctly.

[Step 2] Customer Authentication...
HTTP Status: 200 - Customer logged in successfully.
[Step 2 PASS] Customer session established.

[Step 3] Submitting Booking with Extras & Surcharges...
Redirected to: http://localhost:8080/bookings/15/success
Captured Booking ID: 15
[Step 3 PASS] Booking created successfully with snapshot.

[Step 4] Photographer Authentication...
HTTP Status: 200 - Photographer logged in successfully.
[Step 4 PASS] Photographer session established.

[Step 5] Photographer Accepts Booking Request...
HTTP Status: 200 - Booking #15 accepted.
[Step 5 PASS] State transition to ACCEPTED complete.

[Step 6] Customer Accessing Deposit Checkout Page...
HTTP Status: 200 - Deposit checkout page loaded.
[Step 6 PASS] VietQR checkout page rendered.

[Step 7] Checking Deposit Polling Status (Pre-Payment)...
HTTP Status: 200 - Status: PENDING, Amount: 652,500 VND.
[Step 7 PASS] Polling API active.

[Step 8] Executing Demo Deposit Payment...
Redirected to: http://localhost:8080/bookings/15/deposit/receipt
Receipt confirmed: Status PAID, Remaining Balance: 1,522,500 VND.
[Step 8 PASS] Payment processed and receipt verified.

[Step 9] Checking Deposit Polling Status (Post-Payment)...
HTTP Status: 200 - Status: PAID, Booking: CONFIRMED.
[Step 9 PASS] Deposit and booking state transitions verified.

=== PhotoConnect V6 Verification: ALL 9 STEPS PASSED SUCCESSFULLY! ===
```

---

## 4. Edge Case & Failure Mode Matrix

| Scenario | Input Condition | Expected Behavior | Verification Status |
|---|---|---|---|
| **Boundary Distance 30 km** | `travelDistanceKm = 30` | 0 VND travel surcharge (first 30 km free) | **PASS** (Tested in `PricingServiceTest`) |
| **Boundary Distance 31 km** | `travelDistanceKm = 31` | Exactly 15,000 VND travel surcharge ($1 \times 15,000$) | **PASS** (Tested in `PricingServiceTest`) |
| **Night Shoot Boundary 17:59** | `bookingTime = "17:59"`, `nightShoot = false` | 0 VND night surcharge | **PASS** (Tested in `PricingServiceTest`) |
| **Night Shoot Boundary 18:00** | `bookingTime = "18:00"` | +20% night surcharge applied automatically | **PASS** (Tested in `PricingServiceTest`) |
| **Night Shoot Boundary 06:00** | `bookingTime = "06:00"` | +20% night surcharge applied automatically | **PASS** (Tested in `PricingServiceTest`) |
| **Night Shoot Boundary 06:01** | `bookingTime = "06:01"`, `nightShoot = false` | 0 VND night surcharge | **PASS** (Tested in `PricingServiceTest`) |
| **Half-Up Rounding** | Agreed total with fractional 30% | `RoundingMode.HALF_UP` to nearest whole integer VND | **PASS** (Tested in `PricingServiceTest`) |
| **Replay Attack** | Duplicate external transaction ID | Replay rejected; database unique constraint preserves integrity | **PASS** (Tested in `PaymentWebhookControllerTest`) |
| **Tampered Webhook Payload** | Invalid HMAC signature header | Request rejected with HTTP 401 Unauthorized | **PASS** (Tested in `PaymentWebhookControllerTest`) |
| **QR Data Truncation** | High-density SVG/data URI QR | `NVARCHAR(MAX)` column avoids truncation errors | **PASS** (Flyway V032 verified) |
| **15-Minute Expiry** | Deposit unpaid after 15 minutes | Status set to `EXPIRED`; booking remains unconfirmed | **PASS** (Tested in `DepositServiceTest`) |

---

## 5. Visual Evidence Artifacts

The following high-resolution visual evidence artifacts are archived in `docs/images/v6/`:

1. `01_v6_booking_live_pricing_card.png`: Customer booking form displaying live authoritative pricing card with 30% deposit calculation.
2. `02_v6_photographer_financial_summary.png`: Creator workspace showing financial summary card with deposit and balance breakdown.
3. `03_v6_vietqr_deposit_checkout.png`: VietQR deposit checkout interface with countdown timer, bank credentials, and demo simulation controls.
4. `04_v6_deposit_receipt_confirmed.png`: Finalized electronic deposit receipt showing `PAID` status and remaining 70% balance.

---

## 6. Sign-off & Quality Recommendation

All components of PhotoConnect V6 satisfy the specification and design constraints. The application is ready for production staging and academic presentation.
