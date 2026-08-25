# REALTIME_EVENTS.md — WebSocket / STOMP

## 1. Phạm vi

Realtime chỉ dùng cho:
- Chat.
- Booking notification cơ bản.

Không biến mọi thứ thành realtime để tránh tăng độ phức tạp cho đồ án cá nhân.

---

## 2. Endpoint

WebSocket handshake:

```text
/ws
```

Khuyến nghị Spring WebSocket + STOMP.

Client subscribe:

```text
/topic/booking/{bookingId}/chat
/user/queue/notifications
```

Client send:

```text
/app/chat.send
```

---

## 3. Event `chat.message`

Payload:

```json
{
  "type": "CHAT_MESSAGE",
  "bookingId": 101,
  "messageId": 9001,
  "senderId": 5,
  "receiverId": 8,
  "content": "Chào bạn, lịch 9h nhé.",
  "sentAt": "2026-09-10T08:30:00"
}
```

Flow:

```text
Client SEND /app/chat.send
→ server validate JWT
→ verify user belongs to booking
→ save message DB
→ publish /topic/booking/{id}/chat
```

---

## 4. Event `booking.status_changed`

Destination:

```text
/user/queue/notifications
```

Payload:

```json
{
  "type": "BOOKING_STATUS_CHANGED",
  "bookingId": 101,
  "oldStatus": "PENDING",
  "newStatus": "ACCEPTED",
  "message": "Booking #101 has been accepted"
}
```

---

## 5. Event `photographer.approved`

Payload:

```json
{
  "type": "PHOTOGRAPHER_APPROVED",
  "photographerId": 12,
  "message": "Your photographer profile has been approved"
}
```

---

## 6. Security

- WebSocket connect phải authenticate.
- User không được subscribe tùy ý vào booking không thuộc mình.
- Server luôn validate membership của booking.
- Không tin senderId từ client; lấy từ principal/JWT.
- Giới hạn message, ví dụ 2000 ký tự.

---

## 7. Nếu WebSocket không kịp

Fallback demo:
- Chat polling 3–5 giây.

Nhưng vì đề tài yêu cầu WebSocket, nên ưu tiên hoàn thành ít nhất chat realtime.
