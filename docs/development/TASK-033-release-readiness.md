# TASK-033 — Release Readiness & Final Verification

## Status

Release-readiness implementation and automated/package verification: **PASS**

Final human UI/demo review: **PENDING**

Merge/tag/release: **NOT PERFORMED**

## Release audit

The feature set remains frozen after TASK-030. TASK-031 visual changes and TASK-032 documentation were audited against current controllers, services, repositories, entities, JSPs, CSS/JavaScript, tests, configuration, migrations, and seed scripts. No TASK-034 or further application feature work was created.

## Functional review

- **Guest:** public homepage, approved-only marketplace, filters/pagination, and photographer detail are covered by controller/service tests.
- **Customer:** booking creation/ownership/cancellation, demo payment/result/receipt ownership, review eligibility, and chat participation are covered.
- **Photographer:** onboarding, approved-owner portfolio, owned availability, assigned booking lifecycle, and booking chat are covered.
- **Admin:** dashboard, guarded user status, photographer approval/rejection, booking monitoring, and review hide/unhide are covered.
- Static UI contracts confirm TASK-031 preserved expected route/form/disclosure hooks.

## Security review

- BCrypt login uses server-side HTTP sessions and session-ID rotation.
- Shared controller role checks and service-layer ownership/participant checks cover booking, portfolio, availability, payment, receipt, review, chat, and admin actions.
- Public photographer DTO/query boundaries exclude credentials and unapproved profiles.
- WebSocket origins reject wildcards; WebSocket and REST chat derive identity from the session and validate booking participants.
- A filename-only heuristic scan found no candidate production credential assignments outside documented/test demo material. No secret values were printed or added.

## Payment review

- Deposit is computed on the server as `agreedPrice x 0.30`, with two-decimal rounding.
- Client input cannot set amount, owner, status, or transaction reference.
- One-to-one booking/deposit uniqueness and pessimistic locking protect duplicate processing; paid results are idempotent.
- Result/receipt require the booking customer; legacy paid deposits with nullable metadata remain renderable.
- Demo QR/card are explicitly simulated. No gateway/real transfer exists, and card number/expiry/CVV/cardholder fields have no persistence columns.
- Checkout, result, and receipt preserve the visible demo/no-real-money disclosures.

## Frontend review

- All 27 full-page JSPs and shared navigation fragments were included in TASK-031.
- Script/style opening and closing tag counts match across every JSP.
- `chat.jsp` contains one `currentUserId` declaration and retains STOMP/SockJS plus REST fallback.
- No primary `<button>` or submit input without an intentional class was found.
- No hardcoded root JSP action/link/src reference, Windows project path, or user-visible TODO placeholder was found.
- Expected localhost defaults exist only in environment configuration/test values, not hardcoded navigation links.
- Human pixel/browser verification at 1440/1024/768/390 px remains pending.

## Database review

- Current entities: `User`, `PhotographerProfile`, `PortfolioImage`, `PhotographerUnavailableDate`, `Booking`, `Deposit`, `Message`, and `Review`.
- The repository has no complete V001–V007 SQL migration history; the base development schema has historically used Hibernate `ddl-auto=update`.
- V008 is a guarded review-status migration; V009 is optional guarded demo data; V010 is a forward-only transaction adding nullable payment metadata.
- No historical schema migration was changed during the final batch. Only the V009 seed header's task-number typo was corrected.
- No destructive SQL was added. The Java and SQL seed paths remain opt-in, idempotent, and non-destructive and are now documented as different datasets.

## Documentation review

Reconciled the root README, roadmap, backend/frontend plans, specification, route/API contract, ERD, realtime/security/setup references, architecture, feature matrix, setup guide, demo script/checklist, project status, and TASK-028/TASK-030 records. Final documents consistently state:

- JSP/JSTL + HTML/CSS/plain JavaScript; no React/Vue/Angular/Python/SPA/Node frontend build
- SQL Server persistence
- server-calculated 30% deposit
- explicitly local demo payment with no real transfer
- optional seed behavior and honest manual-verification status

## Automated tests

Pre-modification baseline:

```text
Tests run: 408, Failures: 0, Errors: 0, Skipped: 23
BUILD SUCCESS
```

Final suite:

```text
Tests run: 408, Failures: 0, Errors: 0, Skipped: 23
BUILD SUCCESS
```

The 23 skips are the existing environment-gated SQL Server integration tests. No test was deleted or weakened. TASK-031 added four UI contract tests; TASK-032/033 added no new Java tests because they were documentation/audit work and the existing release suite covered the implementation paths.

## Package

Before cleaning, port 8080 had no listener and no PhotoConnect/Spring Boot/DevTools Java process was present.

```text
mvn clean package
Tests run: 408, Failures: 0, Errors: 0, Skipped: 23
BUILD SUCCESS
target/photoconnect.war: 60,831,790 bytes
```

`target/` is ignored and the WAR is not a source change.

## Startup sanity

Not performed. Local SQL Server port 1433 was reachable, but `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` were not configured in this process. Starting would have required unknown credentials, so no runtime success was fabricated. Cloudinary variables were also unavailable.

## Remaining limitations and human release gate

- Complete the representative viewport and multi-role browser matrix.
- Rehearse booking completion/review, availability blocking, two-session realtime chat, and payment edge/authorization cases.
- Verify real Cloudinary upload/delete only with authorized environment credentials.
- Run the prepared teacher demo end to end and record actual manual results.
- Review the uncommitted diff, then decide whether to commit/merge/tag. This task did not perform any of those Git actions.
