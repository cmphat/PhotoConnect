# API_CONTRACT.md — MVC Routes Và API Contract

> Vì frontend dùng JSP/JSTL, project có cả **MVC routes trả view** và một số **JSON API** cho thao tác async / realtime.

---

## 1. Quy ước response JSON

Success:

```json
{
  "success": true,
  "data": {},
  "message": "OK"
}
```

Error:

```json
{
  "success": false,
  "errorCode": "BOOKING_002_TIME_CONFLICT",
  "message": "Photographer is not available at this time"
}
```

---

# 2. Authentication

## MVC

### GET `/login`
Trang đăng nhập.

### GET `/register`
Trang đăng ký.

## API

### POST `/api/auth/register`

Request:

```json
{
  "fullName": "Nguyen Van A",
  "email": "a@example.com",
  "password": "StrongPassword123",
  "role": "CUSTOMER"
}
```

Photographer được phép đăng ký role `PHOTOGRAPHER`, sau đó profile ở trạng thái `PENDING`.

### POST `/api/auth/login`

Request:

```json
{
  "email": "a@example.com",
  "password": "StrongPassword123"
}
```

Response:
- JWT.
- role.
- basic user info.

Khuyến nghị lưu JWT trong HttpOnly cookie nếu triển khai web MVC.

---

# 3. Photographer

## MVC

### GET `/photographers`
Danh sách + search/filter.

Query:
- `keyword`
- `location`
- `category`
- `minPrice`
- `maxPrice`
- `page`

`page` is zero-based, defaults to `0`, and is validated in the range `0..10000`. The MVC marketplace returns at most 12 approved photographers per page and preserves active filters in pagination links.

### GET `/photographers/{id}`
Chi tiết profile + portfolio + packages + review.

### GET `/photographer/profile`
Trang quản lý profile của photographer.

---

## API

### PUT `/api/photographer/profile`
Role: `PHOTOGRAPHER`

### POST `/api/photographer/portfolio`
Role: `PHOTOGRAPHER`
Content-Type: multipart/form-data

### DELETE `/api/photographer/portfolio/{id}`
Role: owner photographer

### POST `/api/photographer/packages`
Role: `PHOTOGRAPHER`

### PUT `/api/photographer/packages/{id}`
Role: owner photographer

### DELETE `/api/photographer/packages/{id}`
Soft-delete hoặc status `INACTIVE`.

---

# 4. Booking

## MVC

### GET `/bookings`
Danh sách booking của user hiện tại.

### GET `/bookings/{id}`
Booking detail.

---

## API

### POST `/api/bookings`
Role: `CUSTOMER`

Request:

```json
{
  "photographerId": 10,
  "servicePackageId": 21,
  "startTime": "2026-09-10T09:00:00",
  "location": "Thu Duc, HCMC",
  "note": "Graduation photos"
}
```

Server tự tính `endTime` từ `duration_minutes`.

### PATCH `/api/bookings/{id}/accept`
Role: photographer owner.

### PATCH `/api/bookings/{id}/reject`
Role: photographer owner.

### PATCH `/api/bookings/{id}/cancel`
Role: booking customer hoặc rule cho phép.

### PATCH `/api/bookings/{id}/start`
Role: photographer owner.

### PATCH `/api/bookings/{id}/complete`
Role: photographer owner.

---

# 5. Review

### POST `/api/reviews`
Role: `CUSTOMER`

Request:

```json
{
  "bookingId": 100,
  "rating": 5,
  "comment": "Photographer rất nhiệt tình."
}
```

### GET `/api/photographers/{id}/reviews`
Public.

---

# 6. Chat

### GET `/api/bookings/{bookingId}/messages`
Role:
- booking customer
- booking photographer

Pagination bằng `beforeId` hoặc `page`.

### PATCH `/api/messages/{id}/read`
Mark read.

Realtime gửi qua WebSocket, xem `REALTIME_EVENTS.md`.

---

# 7. Admin

## MVC

### GET `/admin`
Dashboard.

### GET `/admin/photographers`
Danh sách photographer chờ duyệt.

### GET `/admin/users`
Danh sách user.

### GET `/admin/bookings`
Danh sách booking.

## API

### PATCH `/api/admin/photographers/{id}/approve`

### PATCH `/api/admin/photographers/{id}/reject`

### PATCH `/api/admin/users/{id}/lock`

### PATCH `/api/admin/users/{id}/unlock`

### PATCH `/api/admin/reviews/{id}/hide`

---

# 8. HTTP status

| Case | Status |
|---|---|
| Success GET | 200 |
| Create success | 201 |
| Validation | 400 |
| Not authenticated | 401 |
| Forbidden | 403 |
| Not found | 404 |
| Conflict | 409 |
| Server error | 500 |

---

# 9. Nguyên tắc

- Không tin ID gửi từ client nếu có thể lấy user từ JWT.
- Authorization luôn kiểm tra server-side.
- Booking status chuyển theo state machine trong `SPEC.md`.
- Error code phải dùng `ERROR_CODES.md`.
