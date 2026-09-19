# PhotoConnect Security Model

## Authentication

- Public registration creates `CUSTOMER` accounts only.
- Passwords are BCrypt hashes; raw passwords are not stored or placed in the session.
- Login uses an application-managed server-side HTTP session and rotates the session ID.
- The session stores user ID, safe display identity, and role name.
- Inactive and banned accounts cannot authenticate.

The application uses `spring-security-crypto` for BCrypt. It does not use JWT and does not currently use a Spring Security filter-chain login.

## Authorization and ownership

Controller role checks are backed by service-layer checks for sensitive operations:

- customers access only owned bookings, deposits, receipts, chats, and eligible reviews;
- photographers access only their profile, portfolio, availability, and assigned bookings/chats;
- admins alone access `/admin/**` mappings;
- public repository queries expose only active, approved photographer DTO data;
- WebSocket and REST chat both validate booking participants and derive the receiver;
- payment amount, owner, status, and reference remain server-authoritative.

## Secrets

Database and Cloudinary credentials are environment variables. Local secret files, `.env*`, logs, build output, and WAR files are ignored. Never commit or print real credential values.

## Payment data

The checkout is a local demo only. No real gateway or transfer exists. Card number, expiry, CVV, and cardholder input are transient and have no persistence columns. Persisted data is limited to the simulated method/status/reference/timestamps and safe failure text.

## Input and upload safety

Bean Validation plus service checks bound account, profile, search, booking, review, chat, payment, availability, and portfolio input. Portfolio uploads accept configured image media types/sizes, store a Cloudinary public ID, and apply owner-only deletion.

## Remaining release review

Automated role/ownership tests are green. A human should still rehearse cross-role/cross-owner routes, WebSocket origin behavior, browser validation messages, and real Cloudinary operations before tagging a release.
