# PhotoConnect Architecture

## System shape

```text
Browser
  |
  | HTTP forms, links, JSON fallback, STOMP/SockJS
  v
JSP/JSTL + HTML/CSS/plain JavaScript
  v
Spring MVC controllers / WebSocket message controller
  v
Services (business rules, authorization, transactions)
  v
Spring Data repositories / JPA / Hibernate
  v
Microsoft SQL Server
```

PhotoConnect is a server-rendered Spring MVC application packaged as a WAR. Controllers select JSP views or return the small JSON chat envelope defined in `docs/API_CONTRACT.md`. It is not a React, Vue, Angular, Python, Node-built, or SPA application.

## Layers

- **Views:** 27 full-page JSPs plus shared navigation fragments. JSTL renders server data; shared CSS and limited plain JavaScript provide responsive behavior, portfolio interaction, checkout behavior, and chat.
- **Controllers:** map public, customer, photographer, admin, payment, review, error, REST-chat, and WebSocket-chat routes. MVC identity always comes from the HTTP session.
- **Services:** enforce lifecycle transitions, role/ownership rules, validation beyond form binding, transaction boundaries, payment calculations, and DTO mapping.
- **Repositories:** Spring Data JPA queries include explicit fetches for `open-in-view=false`, pagination, aggregates, participant lookup, and pessimistic payment locking.
- **Database:** SQL Server persists users, profiles, portfolio metadata, bookings, deposits, unavailable dates, messages, and reviews.

## Authentication and authorization

Login verifies a BCrypt hash and then stores `userId`, `userEmail`, `userFullName`, and the role name in the server-side HTTP session. The session ID is rotated on successful login. Shared role utilities protect controller entry points, while services repeat ownership and participant checks for sensitive operations.

The application uses `spring-security-crypto` for BCrypt, but it does not use JWT and does not currently install a Spring Security filter chain. This distinction is deliberate in the final documentation.

## Portfolio and Cloudinary

Approved photographers upload supported image files through the portfolio service. Cloudinary stores the image binary; SQL Server stores the secure URL, Cloudinary public ID, caption, display order, owner profile, and creation time. Owner checks protect deletion. When a database write fails after remote upload, the service attempts compensating Cloudinary deletion.

Cloudinary is needed only for real upload/delete demonstrations. Existing image URLs can still render without application credentials.

## Realtime chat

- SockJS endpoint: `/ws`; raw WebSocket endpoint: `/ws-raw`
- Client publish destination: `/app/chat.send`
- Subscription destination: `/topic/booking/{bookingId}/chat`
- REST fallback: `GET` and `POST /api/bookings/{bookingId}/messages`

The WebSocket handshake carries the HTTP session. The chat service verifies that the sender is the booking's customer or photographer, derives the receiver, and persists each message. Browser origins come from `WEBSOCKET_ALLOWED_ORIGINS`; wildcards are rejected. If the external SockJS/STOMP browser libraries or connection are unavailable, the JSP uses its context-path-safe REST fallback.

## Demo payment architecture

The payment flow is local simulation only:

1. The authenticated booking customer opens checkout for an `ACCEPTED` booking.
2. The server loads the booking and calculates `agreedPrice x 0.30`, rounded to two decimals.
3. One deposit row per booking is created or reused. A server-generated `PC-yyyyMMdd-XXXXXXXX` reference identifies the attempt.
4. The transient demo request selects Demo QR or Demo Card. It cannot submit an amount, owner, status, or transaction reference.
5. A pessimistic database lock protects processing. Paid requests are idempotent; duplicate submissions reuse the paid state.
6. Only payment method, safe failure reason, status, reference, and paid time may be stored. Card number, expiry, CVV, and cardholder input are never persisted.

No gateway, bank API, merchant account, or real-money transfer exists. The UI consistently labels the environment and states that no real money will be transferred.

## Deployment and context path

JSP URLs, static assets, forms, and JavaScript endpoints use `pageContext.request.contextPath` where required. This preserves WAR deployment under a non-root servlet context. The default development URL remains `http://localhost:8080/`.

## Transaction boundaries

Multi-write booking, deposit, review/rating, availability, and portfolio database operations are service transactions. Read-only queries use read-only transactions where applicable. Cloudinary remains a remote, non-transactional boundary and is handled with explicit compensation rather than pretending it participates in the SQL transaction.
