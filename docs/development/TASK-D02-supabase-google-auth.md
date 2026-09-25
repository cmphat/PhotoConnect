# TASK-D02 — Supabase Auth + Google OAuth Integration

## Status

Implemented in source with mocked automated verification. **Live Google OAuth verification is PENDING** until a human supplies a real Supabase project, Google OAuth credentials, applies V014, and completes the browser checklist. Supabase is an external identity verifier only; PhotoConnect application data remains in SQL Server.

## D01 audit and preserved architecture

D01 uses `JwtAuthenticationFilter` as the authoritative HTTP boundary. It reads the HttpOnly `PHOTOCONNECT_AUTH` cookie, validates the JJWT signature/expiry/claims, reloads the SQL Server user, requires `ACTIVE` status and an exact persisted-role match, publishes `AuthenticatedUser`, and only then mirrors compatibility session fields. `DefaultJwtCookieService` issues `HttpOnly`, `SameSite=Lax`, path-scoped cookies with expiry aligned to the 30-minute application JWT. Logout clears that cookie and invalidates the servlet session.

Email/password authentication still uses BCrypt and is unchanged. CSRF uses Spring Security's session token on unsafe MVC requests. WebSocket/SockJS authentication still begins at the D01 HTTP filter, copies only the validated compatibility session into the handshake, and rechecks booking participation in `ChatService`. Photographer approval remains separate from account role. Existing booking, deposit/receipt, review, saved-photographer, portfolio, availability, dashboard, admin, and chat ownership checks were not replaced.

## Architecture and OAuth flow

```text
Browser -> GET /auth/google
        -> PhotoConnect stores state + PKCE verifier + intent in HttpSession
        -> Supabase /auth/v1/authorize?provider=google (S256 challenge)
        -> Google -> Supabase callback
        -> GET /auth/google/callback?code=...&state=...
        -> PhotoConnect validates and consumes state, then exchanges code + verifier
        -> PhotoConnect GETs /auth/v1/user with the returned short-lived token
        -> verified Google subject -> SQL Server external_auth_identities -> users
        -> existing D01 PhotoConnect JWT cookie -> protected application routes
```

The state, PKCE verifier, purpose (`LOGIN` or `LINK`), linking local user ID, and ten-minute expiry are server-owned. State is compared in constant time and consumed before code exchange, preventing replay through the application callback. The Supabase authorization code is one-time and short-lived. Supabase access/refresh tokens are not persisted, returned to JavaScript, placed in URLs, or treated as PhotoConnect JWTs.

After the code exchange, the server calls Supabase `/auth/v1/user` and requires a confirmed email plus a `google` identity with a stable provider subject. Browser-submitted identity, email, role, metadata, and access tokens are never trusted.

Official references used: [Supabase PKCE flow](https://supabase.com/docs/guides/auth/sessions/pkce-flow), [Google social login](https://supabase.com/docs/guides/auth/social-login/auth-google), [redirect URL configuration](https://supabase.com/docs/guides/auth/redirect-urls), and the [Supabase Auth token API](https://github.com/supabase/auth/blob/master/openapi.yaml).

## Routes and callback

| Route | Purpose |
|---|---|
| `GET /auth/google` | Start Google login/registration through Supabase. |
| `GET /auth/google/callback` | Validate state, exchange the PKCE code, and resolve verified identity. |
| `GET /auth/google/onboarding` | Display server-held verified identity and safe role choice for a new user. |
| `POST /auth/google/onboarding` | Create the local customer account or continue into photographer application. CSRF protected. |
| `GET /auth/google/link` | Start explicit linking; requires a valid PhotoConnect JWT. |

Local default callback URL:

```text
http://localhost:8080/auth/google/callback
```

Production must use its exact HTTPS equivalent. Configure the same value in `SUPABASE_OAUTH_REDIRECT_URI` and the Supabase Auth redirect allow list. The Google Cloud authorized redirect URI is different: it is the Supabase project callback shown on the Supabase Google provider page, normally:

```text
https://<project-ref>.supabase.co/auth/v1/callback
```

## Configuration

| Environment variable | Required for live Google login | Purpose |
|---|---:|---|
| `SUPABASE_URL` | Yes | Exact Supabase project URL. HTTPS required except localhost development. |
| `SUPABASE_ANON_KEY` | Yes | Supabase anon/publishable client key used for Auth API calls. |
| `SUPABASE_OAUTH_REDIRECT_URI` | Yes | Exact PhotoConnect callback URL. |
| `JWT_SECRET` | Yes | Existing PhotoConnect JWT signing key. |
| `JWT_COOKIE_SECURE` | Production | Must be `true` with HTTPS. |

Existing `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and Cloudinary settings are unchanged. A service-role key is not needed and must not be exposed or configured for this flow. Missing Supabase configuration does not break email/password startup; attempting Google login returns a safe configuration error.

## SQL Server identity mapping and migration

Apply `docs/development/migrations/V014__create_external_auth_identities.sql` using the repository's normal forward-only SQL Server process. It creates:

```text
external_auth_identities(id, user_id, provider, provider_subject, created_at)
```

`(provider, provider_subject)` is unique and `user_id` references `users(id)` with cascade delete. The mapping uses Google's verified stable subject (falling back to Supabase's identity ID only if necessary), never email as the permanent identity key. No application table is migrated to Supabase PostgreSQL.

## Account linking security

An unlinked Google identity whose verified email already exists locally is rejected before onboarding. Email equality never attaches the identity or grants the existing account's role. The user sees a safe message, signs in with the existing password, receives the normal PhotoConnect JWT, and is then sent through a fresh explicit `/auth/google/link` flow. Linking requires the same authenticated local user who started the server-held flow and a newly verified Google identity. An identity already owned by another local account and a second Google identity for the same local account are rejected.

## New-user onboarding and roles

The callback stores the verified identity server-side and redirects to role choice; no role query parameter is trusted by the callback. Only `CUSTOMER` and `PHOTOGRAPHER` choices are accepted. The local account is initially created as active `CUSTOMER` with a random unknown BCrypt password. A customer proceeds to the Customer Dashboard. A photographer choice proceeds to the existing `/become-photographer` form; that existing transaction creates the required profile, changes the persisted role to `PHOTOGRAPHER`, rotates the PhotoConnect JWT, and leaves verification `PENDING`. No OAuth metadata can assign `ADMIN`, and unapproved photographers remain outside the approved marketplace/business rules.

## JWT, CSRF, WebSocket, and authorization decisions

- Successful Google authentication calls the existing `JwtCookieService`; there is no second application token implementation.
- Every later protected request is revalidated by D01 against current SQL Server status and role.
- OAuth state protects the cross-site callback; PKCE S256 binds the authorization code to the initiating browser session.
- The onboarding POST uses the existing synchronizer-token CSRF filter.
- `/auth/google/link` is protected by the D01 filter for any active authenticated local user.
- Supabase provider tokens are discarded after `/auth/v1/user` verification.
- WebSocket URLs, STOMP messages, and chat payloads remain token-free; existing D01 handshake and participant checks are unchanged.
- Existing service-level ownership and IDOR protections are unchanged.

## Automated coverage

Focused tests mock all Supabase HTTP responses. They cover authorization URL/PKCE construction, code exchange, verified Google identity retrieval, non-Google rejection, state expiry/mismatch, cancellation, linked login, new-user onboarding, duplicate-email non-takeover, explicit linking, cross-account subject conflicts, active-account enforcement, customer default role, photographer continuation through existing approval onboarding, ADMIN escalation rejection, D01 JWT cookie issuance, email/password/login-link regression, logout regression (existing suite), CSRF on onboarding, protected linking, and existing authorization/ownership regression through the full suite.

Mocked tests do **not** prove live Google authentication.

## Human Supabase setup

1. Create/select a Supabase project; do not create or migrate PhotoConnect application tables there.
2. In Authentication > Providers > Google, enable Google and enter the Google Web client ID/secret.
3. In Authentication > URL Configuration, set the production Site URL and add the exact PhotoConnect callback URL. Add `http://localhost:8080/auth/google/callback` only for local testing.
4. Copy the project URL and anon/publishable key into `SUPABASE_URL` and `SUPABASE_ANON_KEY`.
5. Set `SUPABASE_OAUTH_REDIRECT_URI` to the allow-listed PhotoConnect callback.
6. Never supply the service-role key to this application or browser.

## Human Google Cloud setup

1. Configure Google Auth Platform branding, audience, and the standard `openid`, email, and profile scopes.
2. Create a Web application OAuth client.
3. Add the application origin (for example `http://localhost:8080`) under authorized JavaScript origins as required by Google/Supabase setup.
4. Add the exact Supabase callback shown in the provider page—not the PhotoConnect callback—as the Google authorized redirect URI: `https://<project-ref>.supabase.co/auth/v1/callback`.
5. Put the Google client ID and secret only in the Supabase provider configuration.

## Manual QA checklist — PENDING

1. Apply V014 to a disposable/current SQL Server database and start with all required environment variables.
2. Confirm email/password registration, login, protected navigation, and logout still work.
3. From login and registration, start Google OAuth and confirm the browser reaches the configured Google/Supabase consent flow.
4. Cancel consent and confirm a safe message with no token or exception details.
5. Complete new Google customer registration; confirm one local user/mapping, Customer Dashboard access, and HttpOnly PhotoConnect cookie.
6. Repeat Google login; confirm the same local user is used and no duplicate row is created.
7. Choose photographer; complete the existing creator form; confirm `PENDING`, no public listing before approval, JWT role rotation, then normal approved flow after admin approval.
8. Attempt callback replay, changed state, expired state, missing code, and malformed code; confirm rejection.
9. Use Google with an existing local email; confirm there is no automatic login. Sign in with the password, complete fresh Google verification, and confirm linking.
10. Attempt to link a Google subject already linked elsewhere and a second Google identity; confirm safe rejection.
11. Disable/ban a linked local account and confirm Google login is rejected.
12. Try a submitted `role=ADMIN`; confirm no account is created or promoted.
13. Recheck customer/photographer/admin route isolation and cross-owner booking, receipt, portfolio, availability, review, saved, and chat access.
14. Open two authenticated sessions and confirm STOMP/SockJS chat still authorizes only booking participants with no token in URL/messages.
15. Inspect browser storage/network: no Supabase or PhotoConnect token in local/session storage, page content, query strings, or rendered errors.

## Known limitations

- Live provider behavior remains PENDING until configured and manually verified.
- One in-flight OAuth flow is stored per servlet session; starting another in the same session invalidates the earlier flow safely.
- Supabase sessions are intentionally not retained or refreshed; Google is used only to prove identity for local sign-in/linking.
- OAuth-created accounts have no user-known local password. This repository currently has no password-reset flow; adding one is outside D02.
- The compatibility servlet session remains for legacy controllers and WebSocket handshake data, but D01 JWT/database validation remains authoritative.
