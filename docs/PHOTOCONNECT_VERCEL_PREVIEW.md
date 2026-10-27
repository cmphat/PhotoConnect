# Photo Connect — Vercel container preview

## Deployment approach (October 2026)
Vercel supports `Dockerfile.vercel` as an OCI-backed HTTP Function. This file builds the **actual Java 21 Spring Boot WAR** with JSP/JSTL, not a static copy of the homepage.

**Important:** Do not consider the Vercel preview equivalent to a production Java application server. The repository currently uses server-side session-backed CSRF, HttpSession and an in-memory Spring STOMP simple broker. Function instances may scale down or run concurrently, so session affinity and in-memory chat delivery are NOT guaranteed. Production-quality chat and sessions require architectural changes (durable/session external storage, broker relay) or a host with a stable Java server.

## Preflight
- Confirm `mvn -B clean verify` on the exact commit is green. SQL integration tests with no database are skipped, not passed.
- Create a *separate*, reachable SQL Server **demo database**. Do not point preview at the local/production customer database.
- Verify this Vercel account/team permits container-backed Functions.
- Configure preview environment variable **names** (do not put real values in Git or chat):
  - Required: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`.
  - HTTPS security: `JWT_COOKIE_SECURE=true`.
  - Booking realtime: `WEBSOCKET_ALLOWED_ORIGINS` set to the precise deployed preview origin (avoid wildcard).
  - Keep safe demo mode: `PAYMENT_MODE=DEMO`, `PAYMENT_REAL_ENABLED=false`.
  - Flyway defaults: `FLYWAY_BASELINE_ON_MIGRATE=false`. Apply a reviewed migration plan to existing unversioned databases, **never** blindly baseline them.
  - Email: set mail capture mode appropriately for testing. Real SMTP must use secrets stored in Vercel settings.
  - Cloudinary: required only for real image upload/delivery tests, plus a separately reviewed authenticated delivery/token strategy.
- HTTP endpoint should bind to `PORT` supplied by Vercel; `Dockerfile.vercel` sets `server.port` accordingly.

## Expected smoke checks
1. Vercel deployment is READY; `GET /` renders an actual JSP page over HTTPS.
2. Verify route `/photographers`; if DB is down, deployment is **not** accepted.
3. Check `/login` and CSRF behavior with repeat requests and separate sessions.
4. Verify JWT cookie Secure/SameSite flags; keep signing keys private.
5. Verify `/ws` and two-user STOMP delivery; document limitations caused by in-memory broker.
6. No real payments; do not configure PayOS or SePay real mode for demo.

## Current status
The GitHub branch adds deployment packaging only; no Vercel project URL, successful remote deployment, SQL Server connectivity, or browser functionality is claimed until independently observed.
