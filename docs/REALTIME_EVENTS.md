# PhotoConnect Realtime Chat Contract

Realtime scope is limited to booking chat. Booking notifications and other events are not implemented.

## Endpoints and destinations

```text
SockJS handshake:      /ws
Raw WebSocket:         /ws-raw
Client SEND:           /app/chat.send
Client SUBSCRIBE:      /topic/booking/{bookingId}/chat
REST history/fallback: GET  /api/bookings/{bookingId}/messages
REST send/fallback:    POST /api/bookings/{bookingId}/messages
```

The browser sends only the booking identifier and message content:

```json
{
  "bookingId": 101,
  "content": "See you at 09:00."
}
```

The server derives the sender from the HTTP session and the receiver from the booking. The broadcast/persisted DTO contains message ID, booking ID, sender identity/display name, receiver ID, content, and sent-time fields.

## Flow

```text
HTTP-session-authenticated client SEND /app/chat.send
  -> validate booking participant
  -> derive receiver
  -> persist message in SQL Server
  -> publish /topic/booking/{bookingId}/chat
```

When SockJS/STOMP browser libraries or the connection are unavailable, the page sends through REST and polls history every three seconds.

## Security

- The WebSocket handshake copies the HTTP session; there is no JWT/principal contract.
- Both WebSocket and REST paths validate that the current session user is the booking customer or assigned photographer.
- Client-provided sender/receiver identity is not trusted.
- Message content is required and bounded to 2000 characters.
- `WEBSOCKET_ALLOWED_ORIGINS` is an explicit allowlist; wildcard origins are rejected.
- The topic name is booking-scoped, while authorization remains enforced before persistence/send.
