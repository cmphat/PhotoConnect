# TASK-C03 — Customer Workspace / Customer Dashboard

## Purpose

TASK-C03 adds a canonical, authenticated customer workspace that brings together existing booking, demo-deposit, chat, review, and saved-photographer capabilities. It is a read-only projection over persisted application data; it does not introduce fake analytics, notifications, workflows, or a dashboard database table.

## Route and access

- `GET /customer/dashboard`
- Guests are redirected to `/login` using the current session-auth convention.
- Only `CUSTOMER` may access the route. `PHOTOGRAPHER` and `ADMIN` receive HTTP 403.
- Identity comes exclusively from `SessionSecurityUtils.userId(session)` and `userRole`; no query parameter, form field, or request body customer ID is accepted.
- The service independently verifies that the session user exists and remains an active `CUSTOMER`.

## Data sources

| Workspace area | Persisted source |
|---|---|
| Welcome identity | `users` |
| Summary, attention, booking preview | `bookings` |
| Payment state and authoritative amount | `deposits`; missing-row amount uses the same server-side 30% `DepositService` rule as checkout |
| Review eligibility | `reviews` plus `COMPLETED` booking status |
| Saved preview | `saved_photographers`, restricted to currently `APPROVED` profiles |
| Names, professional details, rates | `photographer_profiles` |
| Card/preview imagery | `portfolio_images`, cover first with ordered fallback |

No message contents are loaded. The workspace only links to the existing participant-protected chat route.

## Attention rules and priority

The focused query returns the current customer's accepted bookings plus completed bookings without a review. The service applies a deterministic maximum of four items in this order:

1. Accepted booking with no deposit or `PENDING` deposit → **Pay Deposit**.
2. Accepted booking with `FAILED` or `CANCELLED` deposit → **Retry Payment**.
3. Accepted booking with `PROCESSING` deposit → **View Payment**.
4. Accepted booking with `REFUNDED` or `FORFEITED` deposit → **View Booking**; checkout is not offered.
5. Future accepted booking with `PAID` deposit → **Open Chat**.
6. Completed booking without an existing review → **Write Review**.

Paid bookings do not receive a payment-required item. Reviewed bookings do not receive another review item. If no rule applies, the UI shows “You’re all caught up.” Raw HTML is never stored in the view model.

## Booking summary

Four customer-scoped counts are shown:

- **Upcoming:** `PENDING` or `ACCEPTED` and booking date today or later.
- **Pending:** actual `PENDING` status.
- **Completed:** actual `COMPLETED` status.
- **Saved:** current customer's saved profiles that are still `APPROVED`.

The full history remains on the existing `/bookings` page.

## Upcoming and recent bookings

The dashboard retrieves at most five bookings. Future active bookings sort first by booking date; recent records follow by update/create time. Each card is a DTO containing only view-safe data: photographer name and cover, date/time, location, actual status, agreed price, deposit state/amount, and an action list selected in the service.

Supported actions reuse existing routes:

- `/bookings/{id}` — details
- `/bookings/{id}/chat` — chat
- `/bookings/{id}/deposit/checkout` — eligible payment/retry
- `/bookings/{id}/deposit/result` — processing result
- `/bookings/{id}/deposit/receipt` — paid receipt
- `/bookings/{id}/review` — completed and not yet reviewed

The JSP renders supplied actions and does not reproduce booking/deposit/review eligibility logic.

## Saved photographers

The preview reuses TASK-C02 and returns at most four of the current customer's newest saved, currently approved photographers. It displays cover/fallback image, professional name, headline, location, rating, and starting price. **View Profile** uses the public approved-profile route and **View All Saved** uses `/customer/saved-photographers`. No second favorites system was introduced.

## Payment, chat, and review integration

- Deposit amounts are never calculated in JavaScript. Existing deposit rows supply their persisted amount; an accepted booking without a deposit row uses the side-effect-free `DepositService.calculateDepositAmount` method shared with checkout.
- A dashboard GET does not create a deposit row or payment reference.
- Demo payment remains demo-only and uses the existing checkout/result/receipt pages and disclosure.
- Chat is not rebuilt. Links use the existing booking-participant authorization service.
- Review prompts require `COMPLETED` and no review for that booking. The existing review service continues to enforce ownership and uniqueness.

## Authorization and downstream ownership

Every dashboard repository query is scoped by the authenticated session user ID. Generated booking IDs come only from those scoped results. Existing downstream services re-check ownership/participation for booking details, deposit checkout/result/receipt, chat, and review. Existing security tests cover cross-customer booking, payment, chat, and review access; no downstream IDOR defect was found during C03.

## Query and performance design

- One user lookup validates the active customer.
- Scalar customer-scoped count queries build the summary.
- `Pageable` limits booking preview to five and saved preview to four.
- Booking queries fetch customer, photographer profile, and photographer user in the same query.
- Attention candidates are restricted in SQL to accepted or unreviewed-completed bookings rather than loading all bookings.
- Deposits and reviewed booking IDs are loaded in bulk for the union of displayed/actionable booking IDs.
- Portfolio image candidates are loaded once for the small union of displayed profile IDs; cover-first ordering provides a deterministic fallback.
- Saved preview fetches profile and user in one query and filters to `APPROVED` in the query, with a defensive service check.
- The aggregation service is `@Transactional(readOnly = true)` and fully maps immutable DTOs before returning, preserving `spring.jpa.open-in-view=false` compatibility.

## Responsive and accessibility behavior

- The workspace uses the existing SiteMesh shell, Bootstrap primitives, and PhotoConnect tokens.
- Desktop summary and saved grids collapse at 900px and 640px; 390px uses one logical column.
- Booking actions wrap on tablet and become full-width stacked controls on mobile.
- Sections use semantic headings, actions are real links, state is written as text rather than color alone, and images include descriptive alt text.
- Typography stays within the existing sans-serif system and avoids a marketing-scale hero.

## Automated tests

- `CustomerDashboardControllerTest`: customer access, guest redirect, photographer/admin denial, session identity, empty rendering.
- `CustomerDashboardServiceTest`: customer-scoped summary, payment required, paid behavior, review eligibility/already-reviewed behavior, approved saved preview, empty state, non-customer defense.
- `CustomerDashboardUiContractTest`: SiteMesh-compatible markup, required sections/routes, server-selected actions, responsive rules, and customer navigation separation.
- `TransactionBoundaryTest`: read-only transaction boundary for open-in-view-disabled DTO mapping.
- Existing booking, deposit/receipt, chat, review, C01 profile, C02 saved, portfolio/Cloudinary, SiteMesh, Bootstrap, availability, and admin suites remain regression coverage.

SQL Server repository integration tests remain environment-gated and must be reported as skipped when credentials are unavailable, not as passing.

## Database

No migration was created. V011, V012, and V013 are unchanged.

## Manual QA — pending

Human verification is pending until performed by the user.

### Customer with data

1. Login Customer.
2. Open `/customer/dashboard`.
3. Verify customer identity/name.
4. Verify booking summary.
5. Verify upcoming/recent bookings.
6. Verify actionable deposit if applicable.
7. Open booking.
8. Open chat.
9. Open payment when eligible.
10. Open receipt when paid.
11. Open review when eligible.
12. Verify Saved preview.
13. Open View All Saved.
14. Open photographer profile.

### New customer

15. Login Customer with no bookings/saved photographers.
16. Verify useful empty state.
17. Verify Explore CTA.

### Security

18. Attempt direct Photographer access to `/customer/dashboard`.
19. Attempt Admin access.
20. Verify unauthorized roles are rejected/redirected safely.

### Responsive

21. Review at 1440px.
22. Review at 1024px.
23. Review at 768px.
24. Review at 390px.
