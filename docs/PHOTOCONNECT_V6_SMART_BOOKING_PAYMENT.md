# PhotoConnect V6 — Smart Booking & VietQR Deposit Architecture Specification

**System:** PhotoConnect — High-End Vietnamese Photography Marketplace  
**Version:** V6.0.0 (Spring Boot 3.3.0, Spring MVC, JSP/JSTL, MS SQL Server 2022)  
**Document Classification:** Technical Architecture & Release Specification  
**Status:** IMPLEMENTED & VERIFIED  

---

## 1. Executive Summary

PhotoConnect V6 integrates an authoritative, server-side dynamic pricing calculation engine with a **mandatory 30% deposit payment system** utilizing the Vietnamese National Standard **VietQR (NAPAS 24/7)**.

Prior to V6, shoot bookings were unconfirmed commitments lacking financial escrow or deposit safeguards. Photographers suffered revenue risks from late client cancellations or no-shows, while clients had no binding guarantee of date reservation.

V6 introduces:
1. **Authoritative Pricing Engine:** Precise math for base packages, travel distances, night shoots, and add-ons, with immutable financial snapshots persisted upon booking creation.
2. **Mandatory 30% Deposit:** Exact 30% downpayment calculation with whole-VND rounding rule (`RoundingMode.HALF_UP`).
3. **VietQR Payment Gateway:** Standards-compliant EMVCo/NAPAS QR generation with dual-mode support (`DEMO` simulation vs. `REAL` PayOS/SePay banking gateway).
4. **Resilient Webhook & Replay Protection:** Cryptographic HMAC signature verification and strict idempotency via `dbo.payment_transactions`.
5. **Real-time Synchronized UX:** Multi-channel notification via WebSocket/STOMP, responsive polling fallback, 15-minute checkout countdown, and financial summary cards.

---

## 2. Pricing Engine & Surcharges Mathematical Specification

All financial calculations are computed authoritatively on the backend by `PricingServiceImpl.java` and exposed via `GET /api/pricing/calculate` for real-time frontend synchronization.

### 2.1 Formula Definitions

$$\text{Agreed Price} = \text{Base Price} + \text{Night Surcharge} + \text{Travel Surcharge} + \text{Extras Fee}$$

$$\text{Deposit (30%)} = \text{ROUND}(\text{Agreed Price} \times 0.30, 0, \text{HALF\_UP})$$

$$\text{Remaining Balance (70%)} = \text{Agreed Price} - \text{Deposit}$$

### 2.2 Surcharge Components

| Surcharge Component | Condition / Trigger | Calculation Formula | Rate / Unit |
|---|---|---|---|
| **Base Price** | Selected `PackageId` or Photographer Hourly Base Rate | Package fixed price or base rate | Determined by creator catalog |
| **Night Shoot Surcharge** | `nightShoot == true` OR shoot time between `18:00` and `06:00` | $\text{Base Price} \times 0.20$ | 20% of base package |
| **Travel Surcharge** | `travelDistanceKm > 30` (first 30 km free of charge) | $(\text{travelDistanceKm} - 30) \times 15,000$ VND | 15,000 VND / km beyond 30 km |
| **Extra Shoot Hours** | `extraHours > 0` | $\text{extraHours} \times 200,000$ VND | 200,000 VND / hour |
| **Extra Edited Photos** | `extraPhotos > 0` | $\text{extraPhotos} \times 50,000$ VND | 50,000 VND / photo |
| **2nd Photographer / Assistant** | `additionalPhotographer == true` | 500,000 VND flat fee | 500,000 VND per booking |

### 2.3 Server-Side Immutability Snapshot

To prevent retroactive package price modifications from invalidating agreed bookings, `dbo.bookings` records a frozen financial snapshot at submission time:
- `base_price DECIMAL(18,2)`: Frozen package/base rate.
- `night_surcharge DECIMAL(18,2)`: Frozen night shoot fee.
- `travel_surcharge DECIMAL(18,2)`: Frozen travel fee.
- `extras_fee DECIMAL(18,2)`: Frozen add-ons fee.
- `agreed_price DECIMAL(18,2)`: Authoritative total.

---

## 3. Database Architecture & Schema Evolution

The database schema is migrated via Flyway scripts:

### 3.1 Migration Scripts Overview

- **`V031__create_vietqr_payment_and_pricing_breakdown.sql`**:
  - Adds snapshot columns to `dbo.bookings`.
  - Adds `provider_name`, `provider_ref_id`, `payment_mode`, `payment_metadata` to `dbo.deposits`.
  - Creates `dbo.payment_transactions` audit log with unique index on `(provider_name, external_transaction_id)`.
- **`V032__alter_deposit_qr_code_url_max.sql`**:
  - Alters `dbo.deposits.qr_code_url` to `NVARCHAR(MAX)` to support inline SVG and Base64 data URIs.
  - Updates `CK_deposits_status` to support: `PENDING`, `PROCESSING`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED`, `FORFEITED`.
- **`V033__update_booking_status_check_constraint.sql`**:
  - Recreates `CK_bookings_status` on `dbo.bookings` to support all 12 state-machine statuses: `PENDING`, `ACCEPTED`, `DEPOSIT_PENDING`, `CONFIRMED`, `SHOOTING`, `POST_PROCESSING`, `DELIVERED`, `COMPLETED`, `REJECTED`, `CANCELLED`, `EXPIRED`, `DISPUTED`.

### 3.2 Audit Log Table (`dbo.payment_transactions`)

```sql
CREATE TABLE dbo.payment_transactions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    deposit_id BIGINT NOT NULL,
    provider_name NVARCHAR(50) NOT NULL,
    external_transaction_id NVARCHAR(100) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency NVARCHAR(10) NOT NULL DEFAULT 'VND',
    status NVARCHAR(30) NOT NULL,
    signature NVARCHAR(500) NULL,
    raw_payload NVARCHAR(MAX) NULL,
    received_at DATETIME2(7) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_payment_transactions_deposit FOREIGN KEY (deposit_id) REFERENCES dbo.deposits(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX UQ_payment_transactions_provider_ext_id
ON dbo.payment_transactions (provider_name, external_transaction_id);
```

---

## 4. VietQR Payment Architecture & Gateway Adapters

### 4.1 VietQR Payload Generation

VietQR adheres to EMVCo Merchant-Presented QR Specifications. The system generates a standardized payload containing:
- Payload Format Indicator (`000201`)
- Point of Initiation Method (`010212` - Dynamic QR)
- Merchant Account Information (NAPAS BIN for MBBank: `970422` + Account Number)
- Transaction Currency (`5303704` - VND)
- Transaction Amount (`54...`)
- Country Code (`5802VN`)
- Additional Data Field (`62...` containing transfer reference: `PC-{date}-{uuid}`)
- CRC16 Checksum (`6304...`)

### 4.2 Gateway Adapter Architecture (`PaymentGatewayAdapter`)

```
               +---------------------------+
               |   PaymentGatewayFactory   |
               +-------------+-------------+
                             |
         +-------------------+-------------------+
         |                   |                   |
+--------v-------+  +--------v-------+  +--------v-------+
|  Demo Adapter  |  | PayOS Adapter  |  | SePay Adapter  |
|  (Default)     |  | (Real Mode)    |  | (Real Mode)    |
+----------------+  +----------------+  +----------------+
```

1. **`DemoPaymentGatewayAdapter` (Default)**:
   - Active when `photoconnect.payment.real-enabled=false`.
   - Generates local QR codes using ZXing with an overlaid "DEMO" watermark.
   - Provides safe, non-monetary simulation for grading and testing.
2. **`PayOSPaymentGatewayAdapter` & `SePayPaymentGatewayAdapter`**:
   - Active when `photoconnect.payment.real-enabled=true`.
   - Integrates with licensed Vietnamese payment aggregators.
   - Communicates over HTTPS to retrieve genuine banking QR links.

### 4.3 Webhook Verification & Replay Protection

When a banking transaction occurs, the gateway dispatches a webhook to `/api/webhooks/payment/{provider}`:
1. **Signature Verification:** The payload is verified using the provider's shared secret and HMAC-SHA256 signature algorithm. Tampered requests are rejected with HTTP 401 Unauthorized.
2. **Replay Check:** `PaymentTransactionRepository.existsByProviderNameAndExternalTransactionId` is queried. Duplicate deliveries return HTTP 200 OK immediately without reprocessing.
3. **Amount Reconciliation:** The received amount must match or exceed the expected 30% deposit.
4. **State Transition:** The `deposit` is marked `PAID`, and the `booking` transitions to `CONFIRMED`.
5. **Real-time Dispatch:** WebSocket/STOMP events and internal push notifications are dispatched to both the customer and the photographer.

---

## 5. End-to-End User Experience & Visual Evidence

### 5.1 Step 1: Customer Booking Form with Authoritative Live Pricing

When configuring the booking, clients select packages and adjust surcharges. The authoritative calculation dynamically displays base price, surcharges, total agreed price, the 30% deposit, and the 70% remaining balance.

![Booking Form Live Pricing Breakdown](file:///E:/Web/PhotoConnect_Project_Docs_Solo/docs/images/v6/01_v6_booking_live_pricing_card.png)

### 5.2 Step 2: Photographer Financial Summary Card

Upon receiving the booking, the photographer reviews the detailed financial breakdown in their studio workspace before accepting the session.

![Photographer Financial Summary Card](file:///E:/Web/PhotoConnect_Project_Docs_Solo/docs/images/v6/02_v6_photographer_financial_summary.png)

### 5.3 Step 3: VietQR Deposit Checkout & Bank Transfer

Once accepted, the client is directed to the checkout page featuring the dynamic VietQR code, bank transfer credentials (MBBank, Account #, Transfer Content), a 15-minute countdown timer, and one-click copy buttons.

![Customer VietQR Deposit Checkout](file:///E:/Web/PhotoConnect_Project_Docs_Solo/docs/images/v6/03_v6_vietqr_deposit_checkout.png)

### 5.4 Step 4: Instant Confirmation & Official Deposit Receipt

Upon payment verification (webhook or demo trigger), the deposit is marked `PAID`, the booking status becomes `CONFIRMED`, and a formal electronic receipt is generated showing payment timestamp and the 70% remaining balance.

![Customer Deposit Receipt Confirmed](file:///E:/Web/PhotoConnect_Project_Docs_Solo/docs/images/v6/04_v6_deposit_receipt_confirmed.png)

---

## 6. Configuration Reference

```yaml
# application.yml
photoconnect:
  payment:
    real-enabled: false               # Default DEMO mode (Safe for university evaluation)
    default-provider: DEMO            # Options: DEMO, PAYOS, SEPAY
    deposit-percentage: 30            # 30% mandatory downpayment
    timeout-minutes: 15               # 15-minute payment window
    bank:
      name: "MB"                      # MBBank (Military Commercial Joint Stock Bank)
      account-number: "999988887777"
      account-name: "PHOTOCONNECT VIETNAM"
    payos:
      client-id: "${PAYOS_CLIENT_ID:demo}"
      api-key: "${PAYOS_API_KEY:demo}"
      checksum-key: "${PAYOS_CHECKSUM_KEY:demo}"
    sepay:
      api-token: "${SEPAY_API_TOKEN:demo}"
      webhook-secret: "${SEPAY_WEBHOOK_SECRET:demo}"
```

---

## 7. Conclusion

PhotoConnect V6 resolves the marketplace trust dilemma by introducing an automated, transparent, and mathematically rigorous booking deposit framework. The integration of VietQR standardizes digital payments for the Vietnamese domestic ecosystem while preserving academic compliance and testing safety through its dual-mode architecture.
