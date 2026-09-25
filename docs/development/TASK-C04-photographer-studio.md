# TASK-C04 — Photographer Studio / Photographer Dashboard

## Scope

TASK-C04 adds one authenticated, operational photographer workspace at `GET /photographer/dashboard`. It aggregates existing profile, portfolio, availability, booking, deposit, rating, and chat capabilities. It does not add analytics, revenue, notification, payment-provider, or database concepts.

## Existing photographer experience audited

The implementation was based on the existing `PhotographerProfile`, `PortfolioImage`, `PhotographerUnavailableDate`, `Booking`, `Deposit`, `Review`, and `Message` models and their repositories/services. Existing photographer routes remain authoritative:

- `/photographer/profile/edit` — professional profile editing and C01 completeness
- `/photographer/portfolio` — Cloudinary portfolio upload, cover, category, and deletion management
- `/photographer/schedule` — unavailable-date management
- `/photographer/bookings` and `/photographer/bookings/{id}` — scoped booking list/detail
- `/photographer/bookings/{id}/accept`, `/reject`, `/complete` — existing state transitions
- `/bookings/{bookingId}/chat` — existing participant-authorized chat
- `/photographers/{id}` — approved public profile, portfolio, and visible reviews
- `/photographer/onboarding-status` — verification status

Actual booking states are `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, and `COMPLETED`. Actual deposit states are `PENDING`, `PROCESSING`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED`, and `FORFEITED`.

## Route and authentication

`PhotographerDashboardController` accepts no identity or role request parameter. It reads the existing `userId` and `userRole` HTTP-session attributes through `SessionSecurityUtils`. Guests are redirected to login; CUSTOMER and ADMIN sessions receive HTTP 403.

The aggregation service independently resolves the profile with `findByUserIdWithUser(sessionUserId)` and verifies the persisted user is active and has the `PHOTOGRAPHER` role. Every subsequent query uses that internally derived profile ID. A photographer session with no profile returns to the existing onboarding-status route.

## Data sources and view model

`PhotographerDashboardView` is an immutable, JSP-safe model. It contains only the professional identity, verification/public-profile state, completeness, structured attention items, bounded request/shoot cards, portfolio summary/previews, availability summary, and centralized rating fields. It does not expose user entities, password/authentication fields, payment references, failure details, or Cloudinary public IDs.

The service is `@Transactional(readOnly = true)`, fully maps the view while the transaction is active, and remains compatible with `spring.jpa.open-in-view=false`.

## Attention rules

Items are deterministic and sorted numerically:

1. `10–11` — `PENDING` booking requests and a remaining-request aggregate link
2. `20` — the nearest accepted shoot occurring within seven days
3. `30` — C01 completeness below 100%
4. `40–41` — empty portfolio or missing cover

Only five items are shown. Each item contains a type, title, plain-text description, CTA label/path/method/style, and priority. Services never provide raw HTML.

## Booking requests and upcoming shoots

The request preview is limited to four oldest `PENDING` bookings for the authenticated profile. It includes customer display name, requested date/time, location, a normalized/truncated safe note, agreed-price snapshot, and status. Accept/reject forms post to the existing photographer booking routes, so `BookingService` remains the sole transition authority.

Upcoming shoots are limited to the nearest four future `ACCEPTED` bookings. They include the minimal booking context, deposit label, scoped booking-detail link, and participant-authorized chat link.

## Deposit awareness

Deposits for the bounded upcoming set are loaded in one bulk query. The Studio renders observation-only labels:

- no deposit row or `PENDING` — Awaiting Customer Deposit
- `PROCESSING` — Customer Deposit Processing
- `PAID` — Deposit Paid
- `FAILED` / `CANCELLED` — failed/cancelled customer-deposit state
- `REFUNDED` / `FORFEITED` — final deposit state

There are no photographer checkout, retry, cancel, receipt, or other payment mutation controls. The existing payment feature remains an explicitly simulated demo.

## Profile, portfolio, availability, and reputation

- Completeness calls `PhotographerProfileService.calculateProfileCompleteness(...)`; no second formula or stored percentage exists.
- Portfolio health uses profile-scoped count, distinct non-null category count, and cover existence queries.
- Up to six real portfolio images use the existing Cloudinary `getThumbnailUrl()` delivery transformation. No image is copied or uploaded.
- Availability uses profile-scoped future blocked-date count and nearest future blocked date, linking to the existing schedule manager.
- Reputation displays the `averageRating` and `reviewCount` maintained by the existing centralized visible-review recalculation. It does not recalculate independently.

## Authorization and downstream IDOR review

The dashboard has no user/profile authority parameter, and all dashboard queries require the internally resolved profile ID. Generated booking/chat/portfolio/schedule links point to existing routes whose service layers recheck ownership or booking participation:

- `BookingService.getBookingForPhotographer` rejects a different photographer.
- `ChatService.getBookingForParticipant` rejects a nonparticipant.
- portfolio mutations use the session user and atomic `imageId + profileId` repository lookup.
- availability mutations derive the profile from the session and delete with `dateId + profileId`.

Existing tests already exercise each downstream ownership boundary; no downstream IDOR defect was found and no unrelated security rewrite was required.

## Query and performance approach

- One profile/user fetch join establishes scope.
- Pending and upcoming bookings use bounded, profile-filtered fetch queries with customer preloaded.
- Upcoming deposits use one `IN` query.
- Portfolio health uses aggregate/existence queries and one six-row preview query.
- Availability uses one count and one first-date query.
- Rating values come from the already-centralized profile fields.
- No load-all filtering, N+1 deposit/portfolio/review queries, cache, or dashboard table was added.

## Navigation, responsive behavior, and accessibility

The photographer navbar now starts with Studio and keeps only Bookings, Portfolio, and Availability as adjacent operational links. Customer Dashboard/Saved controls and admin navigation remain separate. Edit Profile and the approved-only Public Profile link live in Studio to avoid navbar overcrowding.

The page uses the current SiteMesh/Bootstrap/PhotoConnect shell. Its asymmetric desktop layout tightens at 1024px, becomes one column at 768px, and keeps actions/images usable at 390px. Sections use semantic headings, statuses include text, progress has ARIA values, focus uses existing visible focus styles, and portfolio images receive meaningful alt text.

## Automated tests

Focused C04 coverage includes:

- guest/customer/admin/photographer route behavior and session identity
- request identity parameters being ignored
- persisted role validation and Photographer A/B query isolation
- pending-only actions and existing accept/reject routes
- upcoming accepted booking scope
- paid/unpaid deposit labels and absence of payment mutation actions
- direct reuse of C01 completeness
- portfolio count/category/cover/transformed preview scope
- future availability count/next-date scope
- centralized rating/review fields and useful empty state
- read-only transaction/open-in-view compatibility
- SiteMesh, responsive, accessibility, navigation, and observation-only UI contracts
- existing booking/chat/portfolio/availability downstream ownership regression suites

Focused verification: **19 tests, 0 failures, 0 errors, 0 skipped**.

Full `mvn test` verification: **524 tests, 0 failures, 0 errors, 29 skipped, BUILD SUCCESS**. The skipped cases are the existing environment-gated application/SQL repository tests and gated service cases; they are reported as skipped, not passed.

## Database

No schema change is required. No migration was created, and V011/V012/V013 were not modified for C04.

## Manual QA — PENDING

Human QA remains pending until a human performs it. TASK-C03 manual QA also remains pending.

### Photographer with data

1. Log in as a photographer.
2. Open `/photographer/dashboard`.
3. Verify professional identity.
4. Verify profile completeness.
5. Verify portfolio summary.
6. Verify Cloudinary portfolio preview.
7. Verify pending booking requests.
8. Open a booking request.
9. Accept/reject only if the manual tester chooses a safe test booking.
10. Verify upcoming shoots.
11. Verify deposit status.
12. Open a booking.
13. Open chat.
14. Open Manage Portfolio.
15. Open Edit Profile.
16. Open Manage Availability.
17. Open Public Profile.
18. Verify rating/review summary.

### New photographer

19. Verify useful onboarding/empty state.
20. Verify profile CTA.
21. Verify portfolio CTA.
22. Verify availability CTA.

### Security

23. Attempt `/photographer/dashboard` as a CUSTOMER.
24. Attempt `/photographer/dashboard` as a guest.
25. Verify safe rejection/redirect.

### Responsive

26. Review at 1440px.
27. Review at 1024px.
28. Review at 768px.
29. Review at 390px.

## Known limitations

- Human browser/responsive QA is pending.
- Live SQL integration tests remain environment-gated when SQL Server test credentials are absent.
- Real Cloudinary delivery depends on configured external credentials and network access.
- The Studio intentionally shows operational state only; it does not add analytics, notifications, production payments, or a dedicated photographer review page.
