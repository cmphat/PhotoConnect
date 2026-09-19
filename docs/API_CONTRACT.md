# PhotoConnect Route and API Contract

This document records implemented controller mappings. PhotoConnect is primarily a server-rendered Spring MVC application; it does not expose a general REST API for every screen.

## JSON envelope

The chat fallback and JSON error handling use:

```json
{
  "success": true,
  "data": {},
  "message": "Optional message",
  "timestamp": "ISO-8601"
}
```

Errors set `success` to `false`, include a stable `errorCode` and safe `message`, and may include a field-error map.

## Public and authentication routes

| Method | Route | Purpose |
|---|---|---|
| GET | `/` | Homepage |
| GET | `/photographers` | Approved photographer search/filter/pagination |
| GET | `/photographers/{id}` | Approved photographer detail |
| GET | `/register` | Registration form; authenticated users are redirected |
| POST | `/register` | Create a `CUSTOMER` account |
| GET | `/login` | Login form |
| POST | `/login` | BCrypt authentication and session creation |
| POST | `/logout` | Invalidate the session |

Marketplace query fields are `keyword`, `city`, `minPrice`, `maxPrice`, `minExperience`, and zero-based `page`. The MVC page size is 12. Only active, approved photographers are returned.

## Customer booking routes

| Method | Route | Purpose |
|---|---|---|
| GET | `/photographers/{id}/book` | Booking form for an approved photographer |
| POST | `/photographers/{id}/book` | Create a customer-owned `PENDING` booking |
| GET | `/bookings/{id}/success` | Owned booking confirmation |
| GET | `/bookings` | Customer's bookings |
| GET | `/bookings/{id}` | Owned booking detail |
| POST | `/bookings/{id}/cancel` | Cancel an owned pending/accepted booking |

Customer identity is read from the session. The server loads the photographer and snapshots `agreedPrice`; client-submitted customer IDs/prices/statuses are not trusted.

## Photographer routes

| Method | Route | Purpose |
|---|---|---|
| GET/POST | `/become-photographer` | Onboarding form/submission |
| GET | `/photographer/onboarding-status` | Current user's application status |
| GET | `/photographer/portfolio` | Approved owner's portfolio management |
| POST | `/photographer/portfolio/upload` | Validated Cloudinary upload |
| POST | `/photographer/portfolio/{id}/delete` | Owner-only deletion |
| GET | `/photographer/schedule` | Availability management |
| POST | `/photographer/schedule/add` | Add blocked date |
| POST | `/photographer/schedule/remove/{id}` | Remove owned blocked date |
| GET | `/photographer/bookings` | Assigned booking requests |
| GET | `/photographer/bookings/{id}` | Assigned booking detail |
| POST | `/photographer/bookings/{id}/accept` | `PENDING -> ACCEPTED` |
| POST | `/photographer/bookings/{id}/reject` | `PENDING -> REJECTED` |
| POST | `/photographer/bookings/{id}/complete` | `ACCEPTED -> COMPLETED` |

## Demo deposit routes

All routes require the booking's authenticated customer. Unpaid checkout requires `ACCEPTED`; existing paid legacy deposits remain viewable.

| Method | Route | Purpose |
|---|---|---|
| GET | `/bookings/{id}/deposit` | Redirect to checkout |
| GET | `/bookings/{id}/deposit/checkout` | Create/reuse server-calculated 30% deposit and show checkout |
| POST | `/bookings/{id}/deposit/process` | Process Demo QR or Demo Card outcome |
| POST | `/bookings/{id}/deposit/cancel` | Cancel a pending demo attempt |
| GET | `/bookings/{id}/deposit/result` | Owned result page |
| GET | `/bookings/{id}/deposit/receipt` | Owned printable paid receipt |

No request field can override the deposit amount. No real transfer occurs, and no card/CVV data is persisted.

## Reviews

| Method | Route | Purpose |
|---|---|---|
| GET | `/bookings/{id}/review` | Form for the completed booking's customer |
| POST | `/bookings/{id}/review` | Create the booking's single review |

## Chat

| Transport | Destination/route | Purpose |
|---|---|---|
| MVC GET | `/bookings/{bookingId}/chat` | Participant-only chat page/history |
| SockJS | `/ws` | STOMP handshake with HTTP session |
| WebSocket | `/ws-raw` | Raw STOMP-compatible endpoint |
| STOMP SEND | `/app/chat.send` | Validate participant and persist message |
| STOMP SUBSCRIBE | `/topic/booking/{bookingId}/chat` | Booking-scoped delivery |
| REST GET | `/api/bookings/{bookingId}/messages` | Participant-only history/fallback polling |
| REST POST | `/api/bookings/{bookingId}/messages` | Participant-only fallback send |

## Admin routes

Every route requires a session role of `ADMIN`.

| Method | Route | Purpose |
|---|---|---|
| GET | `/admin` | Redirect to dashboard |
| GET | `/admin/dashboard` | Platform metrics |
| GET | `/admin/users` | Search/filter users |
| POST | `/admin/users/{id}/status` | Guarded status update |
| GET | `/admin/photographers` | Filter applications/profiles |
| GET | `/admin/photographers/{id}` | Profile/application detail |
| POST | `/admin/photographers/{id}/approve` | Approve pending application |
| POST | `/admin/photographers/{id}/reject` | Reject pending application |
| GET | `/admin/bookings` | Read-only booking monitoring |
| GET | `/admin/reviews` | Review monitoring/filtering |
| POST | `/admin/reviews/{id}/hide` | Hide visible review and recalculate rating |
| POST | `/admin/reviews/{id}/unhide` | Restore hidden review and recalculate rating |

## Errors and HTTP behavior

- MVC validation failures redisplay forms with server messages or use safe redirect feedback.
- MVC access failures redirect or render the central error experience according to the controller/error handler contract.
- JSON failures use appropriate 4xx/5xx status codes and the standard envelope.
- `/error` maps container errors to the custom JSP without exposing stack traces.

See `docs/ERROR_CODES.md` for stable error codes. Actual role, ownership, lifecycle, and validation rules remain authoritative in services and tests.
