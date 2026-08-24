# CODING_CONVENTION.md — Quy Ước Code Và Git

## 1. Package

```text
com.photoconnect
├── config
├── controllers
├── dto
├── entities
├── enums
├── exceptions
├── repositories
├── security
├── services
└── websocket
```

---

## 2. Naming Java

Class:
`PascalCase`

```text
BookingService
PhotographerController
JwtAuthenticationFilter
```

Method / variable:
`camelCase`

```text
createBooking()
photographerId
```

Enum:
`UPPER_SNAKE_CASE`

```text
PENDING
IN_PROGRESS
COMPLETED
```

---

## 3. Layering

### Controller
- Nhận request.
- Validate input.
- Gọi service.
- Không viết business logic lớn.

### Service
- Business logic.
- Transaction.
- Authorization/ownership logic.

### Repository
- Data access.

### DTO
- Không trả entity trực tiếp nếu dữ liệu nhạy cảm.

---

## 4. Entity

Không dùng entity làm request body trực tiếp.

Ví dụ:

```text
CreateBookingRequest
BookingResponse
UpdateProfileRequest
```

---

## 5. Exception

Dùng exception domain:

```text
ResourceNotFoundException
BusinessException
AccessDeniedException
```

Map về error code trong `ERROR_CODES.md`.

---

## 6. JSP

Structure:

```text
WEB-INF/views/
├── auth/
├── customer/
├── photographer/
├── admin/
└── fragments/
```

Không nhét business logic vào JSP.

---

## 7. CSS/JS

```text
src/main/webapp/assets/
├── css/
├── js/
└── images/
```

Tên file:
- `photographer-list.js`
- `booking-detail.js`
- `admin-dashboard.js`

---

## 8. Git commit

Commit theo chức năng thật.

Tốt:

```text
Add user registration
Create photographer profile
Implement booking conflict validation
Add WebSocket chat
Fix booking status transition
```

Không tốt:

```text
update
abc
fix
123
final
```

---

## 9. Branch

Vì làm một mình, không bắt buộc nhiều branch.

Có thể dùng:
- `main`
- `feature/auth`
- `feature/booking`
- `feature/chat`

Nếu branch làm bạn chậm, dùng `main` nhưng commit nhỏ, rõ ràng vẫn chấp nhận được.

---

## 10. Definition of Done

Một task chỉ được coi là Done khi:
1. Code compile.
2. Chạy được.
3. Test luồng chính.
4. Không còn error IDE.
5. Commit Git.
6. Update docs nếu thay đổi contract/schema/status.
