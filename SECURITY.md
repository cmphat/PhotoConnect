# SECURITY.md — Bảo Mật

## 1. Authentication

- Spring Security.
- JWT.
- Password BCrypt.
- JWT chứa tối thiểu:
  - userId
  - role
  - expiration

Khuyến nghị web MVC:
- JWT lưu HttpOnly cookie.
- Không lưu secret trong JavaScript.

---

## 2. Authorization

### CUSTOMER
- Quản lý dữ liệu của chính mình.
- Booking photographer.
- Review booking của mình.

### PHOTOGRAPHER
- Chỉ sửa profile/portfolio/package của mình.
- Chỉ xử lý booking gửi tới mình.
- Chỉ chat booking của mình.

### ADMIN
- Quản trị toàn hệ thống.

Không chỉ ẩn nút trên frontend; backend phải kiểm tra quyền.

---

## 3. Ownership checks

Các service phải có hàm kiểm tra ownership, ví dụ:

```text
booking.customer.id == currentUser.id
booking.photographer.user.id == currentUser.id
portfolio.photographer.user.id == currentUser.id
```

---

## 4. SQL Server Authentication

Không hard-code:

```text
sa
password thật
```

Tạo login riêng `photoconnect`.

---

## 5. Secrets

Không commit:

- DB password
- JWT secret
- Cloudinary API secret

`.gitignore`:

```gitignore
.env
application-local.properties
application-secret.properties
secrets.properties
```

---

## 6. Validation

- Email hợp lệ.
- Password minimum length.
- Price >= 0.
- Rating 1..5.
- Booking start time trong tương lai.
- File upload giới hạn loại và dung lượng.
- Message không rỗng và giới hạn chiều dài.

---

## 7. File upload

Cloudinary:
- Chỉ nhận JPG/JPEG/PNG/WebP.
- Giới hạn dung lượng.
- Không dùng filename từ client làm path hệ thống.
- Lưu `public_id` để xóa ảnh đúng.

---

## 8. CSRF / JWT

Nếu JWT qua cookie:
- Xem xét CSRF protection.
- SameSite cookie.
- Secure cookie khi deploy HTTPS.

Cho demo local có thể cấu hình đơn giản hơn nhưng phải hiểu lý do.

---

## 9. Logging

Không log:
- password
- JWT raw
- API secret

Có thể log:
- userId
- action
- bookingId
- status changes

---

## 10. GitHub checklist

Trước mỗi push:

```bash
git status
git diff --cached
```

Kiểm tra không có:
- password
- secret
- token
