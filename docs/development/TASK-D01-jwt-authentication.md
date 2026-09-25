# TASK-D01 — JWT Authentication Integration and Security Hardening

## Status

Implemented in source with automated verification. The 29-step browser checklist below remains **PENDING** until a human performs it. This task does not add refresh tokens, token persistence, or a database migration.

## Previous authentication audit

Before D01, `LoginController` authenticated an email/password through `AuthService`, which used BCrypt and rejected every account not in `ACTIVE` state. Successful login rotated the servlet session ID and stored `userId`, `userEmail`, `userFullName`, and `userRole` in `HttpSession`. Controllers and `SessionSecurityUtils` read those values directly. Logout invalidated the session. There was no remember-me implementation, JWT dependency, bearer-token path, authentication filter, or CSRF filter.

The actual persisted roles are `CUSTOMER`, `PHOTOGRAPHER`, and `ADMIN`; account states are `ACTIVE`, `INACTIVE`, and `BANNED`. Photographer verification (`PENDING`, approval/rejection) is a separate domain rule. Existing services already enforce booking, payment/receipt, review, saved-photographer, portfolio, availability, Studio, and chat participant/owner boundaries.

Chat used `HttpSessionHandshakeInterceptor`: the WebSocket handshake copied the session identity and `ChatService` rechecked participant ownership and active role. Allowed WebSocket origins were already explicit and wildcard origins were rejected.

## New authentication architecture

After successful credential validation, the server creates a signed JWT and sends it in the `PHOTOCONNECT_AUTH` cookie. `JwtAuthenticationFilter` is the authoritative boundary on every request:

1. Read the HttpOnly cookie.
2. Verify its HMAC signature, structure, required claims, and expiration.
3. Load the subject user from the database.
4. Require an `ACTIVE` account and require the persisted role to match the signed role claim.
5. Publish an immutable `AuthenticatedUser` in the request through `CurrentUserContext`.
6. Populate legacy session display/compatibility fields only from that validated persisted user.
7. Enforce broad route authentication/role boundaries before MVC controllers run.

Missing, expired, malformed, unsupported, wrongly signed, or tampered tokens are anonymous. A stale cookie is expired. Protected HTML requests redirect to `/login`; protected JSON/WebSocket requests receive `401`; authenticated role mismatches receive `403`. Authentication responses use `Cache-Control: no-store`.

## JWT library and claims

D01 uses JJWT 0.13.0 with one API dependency and runtime implementation/Jackson modules. No cryptography is implemented locally.

Claims are intentionally minimal:

- `sub`: persisted numeric user ID
- `role`: server-derived persisted role
- `iat`: issuance time
- `exp`: expiration time
- `jti`: random token identifier

Passwords, hashes, email, phone, profile, booking, and payment data are not included.

## Signing and expiration configuration

`JWT_SECRET` is mandatory. It must be valid Base64 that decodes to at least 32 random bytes. Startup fails clearly if it is absent, malformed, or weak; its value is never logged. Generate and store a unique production secret in the deployment secret manager, not source control.

Access-token lifetime is `PT30M` (30 minutes), configured by `photoconnect.jwt.expiration`. This bounds stolen-cookie exposure while remaining usable for a server-rendered educational application. The service refuses non-positive values and values over 24 hours. Expiration is verified server-side and re-login is required after expiry.

Automated tests receive an explicit, non-production test-only key through Maven Surefire.

## Cookie security

The authentication cookie is:

- `HttpOnly=true` so browser JavaScript cannot read it
- `SameSite=Lax`, appropriate for same-site JSP navigation while reducing cross-site sending
- `Path=/` (or the deployed servlet context path)
- `Max-Age=1800`, aligned to the token lifetime
- `Secure` controlled by `JWT_COOKIE_SECURE`; it must be `true` for HTTPS deployments and may be `false` only for local HTTP development

The token is not stored in local/session storage, sent in a query string, exposed to frontend JavaScript, or logged.

## Login, role promotion, and logout

The existing login UI, BCrypt credential verification, account-state check, session-ID rotation, and redirect remain. CUSTOMER, PHOTOGRAPHER, and ADMIN cookies are produced only from the authenticated persisted `User`; a request cannot provide its role. Failed validation or credentials issue no token.

Photographer onboarding atomically promotes the persisted role. The controller immediately rotates the JWT with the new persisted `PHOTOGRAPHER` role so the old `CUSTOMER` claim cannot compete with the database.

Logout expires the JWT cookie and invalidates the compatibility session. Subsequent protected server requests therefore fail even if a browser displays a previously cached page snapshot.

## Central context and session compatibility

`CurrentUserContext` resolves only the request attribute created by the JWT filter. C03 `/customer/dashboard` and C04 `/photographer/dashboard` use it directly and ignore request-supplied IDs/roles.

Legacy controllers and JSP navigation still read `userId`, `userEmail`, `userFullName`, and `userRole` from the session. These values are no longer accepted as independently authenticated identity: the filter removes them when no valid JWT exists and rewrites them from the current persisted user when one does. If an existing session ID/role conflicts with the validated JWT, both identities are cleared and the request is rejected rather than choosing either one. Remaining session use is compatibility display/controller plumbing and WebSocket handshake propagation, not the authoritative credential.

## Authorization and ownership

The filter applies coarse route boundaries:

- ADMIN: `/admin` and `/admin/**`
- CUSTOMER: `/customer/**`, `/become-photographer`, and photographer book/save/unsave actions
- PHOTOGRAPHER: `/photographer/**`
- any authenticated user, followed by controller/service checks: `/bookings/**`, `/api/bookings/**`, `/ws/**`, `/ws-raw/**`

Home, login, registration, photographer discovery/detail, static assets, and error handling remain public. A valid token never grants access to another user's resources: existing booking ownership, chat participation, deposit/receipt ownership, review eligibility, saved-photographer ownership, portfolio ownership, and availability ownership checks remain in their services.

Every authenticated request reloads the user and rejects inactive/banned accounts or a changed persisted role. Photographer approval requirements remain enforced by the existing marketplace/upload/business rules; JWT does not turn a pending photographer into an approved one.

## CSRF strategy

Because browsers automatically send an HttpOnly cookie, D01 adds Spring Security Web's `CsrfFilter` with `HttpSessionCsrfTokenRepository`. Every unsafe MVC method requires the synchronizer token. All POST JSP forms render the `_csrf` hidden value, and the chat REST fallback sends the token header exposed through a non-sensitive meta tag. Missing/incorrect tokens return `403` (JSON for API paths).

SockJS/WebSocket transport endpoints are the narrow exception because their internal POST transports do not submit MVC form tokens. They remain protected by mandatory valid JWT authentication, current database account/role validation, explicit allowed origins, safe cookie SameSite behavior, and service-level booking-participant checks.

## WebSocket compatibility

The JWT filter runs before the WebSocket handshake and derives the compatibility session identity only from the validated cookie. `HttpSessionHandshakeInterceptor` then copies that validated identity for the existing STOMP controller. `ChatService` still revalidates the sender as an active booking participant. No token is added to a URL, STOMP payload, chat message, or log.

## Environment variables

| Variable | Required | Purpose |
|---|---:|---|
| `JWT_SECRET` | Yes | Base64 key decoding to at least 32 random bytes; startup fails without it |
| `JWT_COOKIE_SECURE` | Production | Set `true` behind HTTPS; defaults to `false` for local HTTP only |

The committed properties set the duration to 30 minutes. Existing database, Cloudinary, payment-simulation, and WebSocket-origin variables are unchanged.

## Automated tests

Focused D01 coverage verifies valid creation/parsing, subject, role, issuance/expiration/JTI, expiry rejection, invalid signature, malformed/tampered input, weak/missing configuration, cookie flags/lifetime/path, logout expiry, CUSTOMER/PHOTOGRAPHER/ADMIN login issuance, invalid-login non-issuance, role boundaries, guest/inactive rejection, conflicting-session rejection, JSON/WebSocket `401`, CSRF rejection and valid form submission, and SockJS CSRF compatibility. Existing service/controller tests continue to cover ownership/IDOR for bookings, receipts, chat, portfolio, availability, reviews, saved photographers, dashboards, and admin behavior.

Database-backed integration tests remain environment-gated and must be reported as skipped unless their SQL Server prerequisites are available.

## Manual QA — PENDING

1. Login CUSTOMER.
2. Verify normal redirect.
3. Open Customer Dashboard.
4. Refresh page.
5. Verify still authenticated.
6. Logout.
7. Attempt Customer Dashboard again.
8. Verify login required.
9. Login Photographer.
10. Open Studio.
11. Open Portfolio.
12. Open Availability.
13. Open Booking.
14. Open Chat.
15. Customer attempts Photographer Studio.
16. Photographer attempts Customer Dashboard.
17. Non-admin attempts Admin.
18. Verify authentication survives normal navigation/refresh.
19. Verify logout removes authentication.
20. In browser devtools, confirm the auth cookie is HttpOnly.
21. Save/unsave photographer.
22. Booking.
23. Demo deposit/payment.
24. Receipt.
25. Review.
26. Portfolio upload.
27. Cover selection.
28. Availability.
29. Chat.

## Known limitations

- No refresh token or revocation table is added; expiration, account state, persisted role checks, and logout cookie deletion define the bounded session. A stolen valid cookie remains usable until expiry unless the account is disabled/banned or its role changes.
- HTTPS deployments must explicitly set `JWT_COOKIE_SECURE=true`; local HTTP defaults to false.
- Compatibility session fields remain until legacy controllers/JSPs are migrated incrementally.
- WebSocket/SockJS transport uses the compatibility session established by the validated handshake; chat authorization remains service-side.
- Human browser QA and environment-dependent SQL Server/Cloudinary checks remain pending.

## Database impact

No database migration. V011, V012, and V013 are unchanged. No token or refresh-token table is created.
