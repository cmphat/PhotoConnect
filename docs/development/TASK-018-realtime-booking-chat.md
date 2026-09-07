# TASK-018 Development Notes: Real-Time Booking Chat (WebSocket & Message Persistence)

## Overview

TASK-018 establishes real-time messaging between customers and photographers directly bound to their confirmed shoot bookings. The implementation provides:
1. Direct WebSocket communication using STOMP over SockJS (`/ws`).
2. Persistent database storage in SQL Server (`messages` table).
3. Eager JPA fetching to prevent N+1 queries and avoid `LazyInitializationException` with `open-in-view=false`.
4. Session-derived authorization ensuring only the booking's customer and photographer can view history or send messages.
5. Automatic REST API fallback (`/api/bookings/{id}/messages`) ensuring message synchronization works in offline and restricted firewall environments.

---

## 1. Core Architecture & Entity Design

### `Message` Entity
Mapped to database table `messages`:

| Column Name | Type | Constraints / Relations | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY IDENTITY | Unique identifier |
| `booking_id` | BIGINT | NOT NULL, FK → `bookings(id)` | Shoot booking associated with conversation |
| `sender_id` | BIGINT | NOT NULL, FK → `users(id)` | Sender user account |
| `receiver_id` | BIGINT | NOT NULL, FK → `users(id)` | Recipient user account |
| `content` | NVARCHAR(2000) | NOT NULL | Message text (trimmed, max 2000 chars) |
| `is_read` | BIT | NOT NULL | Read tracking (default false) |
| `sent_at` | DATETIME2 | NOT NULL | Creation timestamp |

- **Index**: `idx_messages_booking_sent_at` on `(booking_id, sent_at)` ensures efficient chronological message retrieval.

---

## 2. Business Rules & Protections

### 1. Booking Association
- Every message is tied to an existing `Booking`.
- If the booking ID is invalid or not found, an `InvalidBookingException` is thrown.

### 2. Authorization & Ownership
- Only the customer (`booking.customer.id == session.userId`) or the photographer (`booking.photographerProfile.user.id == session.userId`) can view the chat history or send messages (`ChatAccessDeniedException`).
- Sender identity is always derived from the server session attribute `userId` (never trusted from request payloads).

### 3. Recipient Resolution
- The recipient is determined automatically by the server:
  - If the sender is the customer, the recipient is the photographer's `User`.
  - If the sender is the photographer, the recipient is the customer's `User`.

### 4. Validation & Length Limits
- Blank or whitespace-only messages are rejected (`IllegalArgumentException`).
- Content cannot exceed 2000 characters.

### 5. Read Tracking
- When a participant opens the chat view or fetches history via API, any unread messages addressed to them are marked as read (`is_read = true`).

---

## 3. Layered Implementation Breakdown

### Service Layer (`ChatService` / `ChatServiceImpl`)
- Injects `SimpMessageSendingOperations` to broadcast outgoing messages to `/topic/booking/{bookingId}/chat`.
- Manages transactional operations for saving messages and updating read status.

### Presentation & Controllers
- `ChatController`:
  - `GET /bookings/{bookingId}/chat`: Resolves partner identity, loads message history, and renders `chat.jsp`.
- `ChatApiController`:
  - `GET /api/bookings/{bookingId}/messages`: Returns message history JSON.
  - `POST /api/bookings/{bookingId}/messages`: REST API fallback for sending messages.
- `WebSocketChatController`:
  - `@MessageMapping("/chat.send")`: Handles STOMP message submissions from the client.

### WebSocket Configuration (`WebSocketConfig`)
- Registers STOMP endpoint `/ws` with SockJS and `HttpSessionHandshakeInterceptor` to copy HTTP session attributes.
- Configures `/topic` and `/queue` simple broker and `/app` application destination prefix.

### UI & Styling (`chat.jsp`)
- Designed using the shared editorial aesthetic (`photoconnect.css`, `#101014` dark panel, gold accents `#c9a96e`, Inter typography).
- Displays message bubbles (right-aligned for user, left-aligned for partner).
- Includes real-time STOMP client with automatic REST polling fallback if WebSocket is blocked or offline.

---

## 4. Automated Testing & Verification

- **DTO Validation Tests (`SendMessageRequestTest`)**: 5 unit tests covering valid input, blank content, null content, and max length bounds.
- **Service Unit Tests (`ChatServiceTest`)**: 11 unit tests covering customer send, photographer send, history retrieval, automatic mark-as-read, unauthorized access, blank content rejection, oversized content rejection, and WebSocket broadcast invocation.
- **MVC Controller Tests (`ChatControllerTest`)**: 5 WebMvcTest tests covering unauthenticated redirects, authorized customer view, authorized photographer view, and unauthorized access handling.
- **REST Controller Tests (`ChatApiControllerTest`)**: 6 WebMvcTest tests covering unauthenticated rejection, authorized GET history, authorized POST creation, and validation errors.
- **Full Suite**: 219 tests pass (0 failures, 0 errors, 22 skipped integration tests requiring live SQL Server).

---

## 5. Human Verification Checklist (Status: PENDING)

- [ ] 1. Log in as a `CUSTOMER` account.
- [ ] 2. Navigate to an existing booking (`/bookings/{id}`).
- [ ] 3. Click "💬 Open Chat" to load `/bookings/{id}/chat`.
- [ ] 4. Confirm the chat page renders with booking details and "Live (WebSocket)" or "Connected (REST Mode)" status.
- [ ] 5. Send a message: "Hello! Looking forward to our shoot."
- [ ] 6. Confirm message appears immediately in the conversation thread.
- [ ] 7. In a separate browser or incognito window, log in as the `PHOTOGRAPHER` of that booking.
- [ ] 8. Navigate to `/bookings/{id}/chat` (or via `/photographer/bookings/{id}`).
- [ ] 9. Confirm the customer's message is visible and marked as read.
- [ ] 10. Send a reply: "Thank you! See you at the scheduled time."
- [ ] 11. Confirm the customer's browser receives the message in real time without refreshing.
