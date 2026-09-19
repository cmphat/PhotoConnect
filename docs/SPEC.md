# PhotoConnect Functional Specification

## 1. Product

PhotoConnect is a photography marketplace where guests discover approved photographers, customers request and manage shoots, photographers manage their professional presence and assigned bookings, and administrators moderate the platform.

The final course implementation is server-rendered Java: Spring Boot/MVC/Data JPA, SQL Server, JSP/JSTL, HTML, CSS, plain JavaScript, and STOMP/SockJS. React, Vue, Angular, Python, an SPA architecture, and a Node frontend build pipeline are outside the allowed stack.

## 2. Roles

- `CUSTOMER`: default public registration role; owns bookings, payments, chats, and reviews.
- `PHOTOGRAPHER`: a user with a photographer profile; public/operational actions also require appropriate verification state.
- `ADMIN`: platform management role.
- Guest is an unauthenticated browser, not a persisted role.

Account status is `ACTIVE`, `INACTIVE`, or `BANNED`. Photographer verification is independently `PENDING`, `APPROVED`, `REJECTED`, or `SUSPENDED`.

## 3. Implemented scope

### Authentication

- Register a validated customer account with unique normalized email and BCrypt password.
- Log in through server-side HTTP session; rotate the session ID; reject inactive/banned accounts.
- Log out by invalidating the session.
- Enforce roles and ownership in controllers/services, not only in JSP visibility.

### Photographer onboarding and marketplace

- An authenticated customer may create at most one photographer profile and becomes role `PHOTOGRAPHER` with verification `PENDING`.
- Admin may approve or reject a pending application.
- Only active, `APPROVED` profiles appear publicly.
- Guests may search/filter by keyword, city, price, and experience and page through 12 results at a time.
- Public details include profile data, Cloudinary portfolio URLs, visible reviews, and aggregate rating.

### Portfolio and availability

- An approved photographer may upload validated JPG/JPEG/PNG/WebP images up to the configured request limit and delete only owned images.
- Cloudinary holds binaries; SQL Server holds metadata.
- A photographer may add/remove unique unavailable dates.
- Booking creation rejects a blocked date.

### Booking

- Only a `CUSTOMER` may book an active, approved photographer other than themselves.
- Date/time must be future-valid and the date must be available.
- Customer identity and price are server-derived; `agreedPrice` is snapshotted from the profile.
- Customer sees only owned bookings and may cancel `PENDING` or `ACCEPTED` bookings.
- Assigned photographer sees only their bookings and may accept/reject `PENDING` or complete `ACCEPTED` bookings.

Lifecycle:

```text
PENDING -> ACCEPTED -> COMPLETED
PENDING -> REJECTED
PENDING or ACCEPTED -> CANCELLED (customer)
```

### Demo deposit checkout

- Checkout is available to the booking customer for an `ACCEPTED` booking.
- Deposit amount is computed on the server as `agreedPrice x 30%`, rounded to two decimals.
- One deposit belongs to one booking.
- Demo QR and Demo Card produce local deterministic outcomes and a server-generated reference.
- Paid processing is idempotent and protected by pessimistic locking.
- Result and receipt remain owner-only; compatible legacy paid deposits may have nullable metadata.
- No real money, bank API, gateway, or merchant account is involved. Card/CVV input is not persisted.

Deposit lifecycle used by the demo:

```text
PENDING -> PROCESSING -> PAID
PENDING -> PROCESSING -> FAILED
PENDING -> CANCELLED
FAILED or CANCELLED -> PENDING (fresh attempt)
```

Legacy enum values `REFUNDED` and `FORFEITED` remain readable but are not new demo actions.

### Chat

- Only the booking customer and assigned photographer may view/send messages.
- Sender identity comes from the HTTP session; receiver is derived from the booking.
- Messages persist in SQL Server.
- STOMP/SockJS delivers live messages; REST GET/POST provides a fallback.
- WebSocket origins use an explicit configurable allowlist.

### Reviews

- Only the booking's customer may review after `COMPLETED`.
- One review is allowed per booking; rating is 1–5; optional comment is bounded.
- Admin may hide/unhide reviews.
- Public list and cached photographer rating/count use only `VISIBLE` reviews.

### Administration

- Dashboard aggregates users, profiles, booking states, reviews, and clearly disclosed simulated-deposit metrics.
- User search/filter and status updates include self-protection for the logged-in admin.
- Photographer list/detail supports verification filtering and pending approve/reject.
- Booking monitoring is read-only.
- Review moderation supports status filtering and rating recalculation.

## 4. Data model

Implemented entities are `User`, `PhotographerProfile`, `PortfolioImage`, `PhotographerUnavailableDate`, `Booking`, `Deposit`, `Message`, and `Review`. See `docs/ERD.md`.

## 5. Non-functional requirements

- Context-path-safe JSP forms, links, assets, and JavaScript endpoints.
- Server validation with safe user-facing errors; no stack traces in the custom error view.
- BCrypt password storage; no committed database/Cloudinary secrets.
- `open-in-view=false` with explicit repository fetch plans.
- Transactional multi-write services and explicit compensation for Cloudinary's remote boundary.
- Responsive UI designed for representative 1440, 1024, 768, and 390 px widths.
- Keyboard-visible focus, associated labels, meaningful action text, alt text, and semantic status text.

## 6. Frozen exclusions

The final application does not implement service packages, categories, booking status-history entities, advanced notifications, vouchers, AI features, JWT authentication, production payment processing, or a frontend framework migration. No feature task follows TASK-033.

## 7. Completion criteria

- Feature and implementation scope complete through TASK-033.
- `mvn test` and `mvn clean package` complete with zero failures/errors.
- WAR artifact exists.
- Final architecture, feature, setup, database, demo, and status documents agree with source.
- Final human UI/demo review remains explicitly pending until performed.
