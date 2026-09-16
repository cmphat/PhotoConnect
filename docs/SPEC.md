# SPEC.md — Đặc Tả Nghiệp Vụ

**Dự án:** PhotoConnect  
**Loại đồ án:** Cá nhân — 1 người  
**Thời gian:** 8 tuần  
**Mục tiêu:** Xây dựng nền tảng đặt lịch và kết nối nhiếp ảnh gia với khách hàng.

> File này là nguồn tham chiếu nghiệp vụ gốc. Mọi file kỹ thuật khác phải khớp với role, status và entity được định nghĩa tại đây.

---

## 1. Bối cảnh

Khách hàng hiện thường tìm photographer qua Facebook, Instagram hoặc người quen. Quá trình này có các vấn đề:

1. Khó so sánh giá, phong cách và độ uy tín.
2. Trao đổi lịch chụp thủ công, dễ trùng lịch.
3. Không có một nơi tập trung portfolio, gói dịch vụ và đánh giá.
4. Photographer khó quản lý booking và khách hàng.
5. Thiếu cơ chế quản lý trạng thái buổi chụp và lịch sử giao dịch.

PhotoConnect giải quyết bằng một nền tảng có hồ sơ photographer, portfolio, booking, chat và review.

---

## 2. Vai trò

| Role | Mô tả |
|---|---|
| `CUSTOMER` | Tìm photographer, đặt lịch, chat, theo dõi booking, đánh giá |
| `PHOTOGRAPHER` | Quản lý hồ sơ, portfolio, dịch vụ, lịch và booking |
| `ADMIN` | Duyệt photographer, quản lý user/booking/review, xem thống kê |

---

## 3. Phạm vi MVP

### 3.1 Authentication
- Đăng ký bằng email/password.
- Đăng nhập.
- JWT.
- Phân quyền theo role.
- Password lưu BCrypt.
- Photographer mới đăng ký phải chờ admin duyệt.

### 3.2 Photographer
- Hồ sơ cá nhân.
- Avatar.
- Bio.
- Khu vực hoạt động.
- Số năm kinh nghiệm.
- Giá khởi điểm.
- Portfolio.
- Gói dịch vụ.
- Trạng thái duyệt.

### 3.3 Search
Khách có thể:
- Xem danh sách photographer.
- Tìm theo tên.
- Lọc theo khu vực.
- Lọc theo thể loại.
- Lọc theo khoảng giá.
- Xem rating.

### 3.4 Booking
Khách:
- Chọn photographer.
- Chọn gói dịch vụ.
- Chọn ngày giờ.
- Nhập địa điểm.
- Gửi yêu cầu booking.

Photographer:
- Accept.
- Reject.
- Chuyển trạng thái theo tiến độ.

Hệ thống:
- Kiểm tra trùng lịch cơ bản.
- Lưu lịch sử trạng thái.

### 3.5 Chat
- Chat realtime giữa customer và photographer.
- Chat gắn với booking.
- Lưu message vào SQL Server.
- Có trạng thái đã đọc/chưa đọc ở mức cơ bản.

### 3.6 Review
- Chỉ booking `COMPLETED` mới được review.
- Mỗi booking chỉ được review một lần.
- Rating 1–5.
- Cập nhật average rating của photographer.

### 3.7 Admin
- Danh sách user.
- Khóa/mở user.
- Duyệt/từ chối photographer.
- Xem booking.
- Ẩn review không phù hợp.
- Dashboard cơ bản: user count, photographer count, booking count, completed booking count.

### 3.8 Demo deposit checkout
- Booking `ACCEPTED` requires a server-calculated 30% deposit.
- Demo QR and demo card methods are local simulations only; no real payment gateway or money transfer.
- Only the authenticated `CUSTOMER` who owns the booking may initiate or complete checkout.
- Successful demo payment records `PAID`; failed and cancelled attempts never do.

---

## 4. Ngoài phạm vi bắt buộc

Không làm trước khi MVP hoàn thành:

- Thanh toán thật.
- Ví tiền.
- Hoàn tiền tự động.
- Tranh chấp phức tạp.
- App mobile.
- Microservices.
- Recommendation ML.
- AI nhận xét ảnh.
- Chống trao đổi số điện thoại ngoài nền tảng bằng AI.

Các mục này chỉ là bonus.

---

## 5. Luồng chính end-to-end

### 5.1 Photographer onboarding

```text
Register PHOTOGRAPHER
    ↓
Create photographer profile
    ↓
PENDING approval
    ↓
Admin APPROVE / REJECT
    ↓
APPROVED → xuất hiện trong search
```

### 5.2 Booking

```text
Customer xem Photographer
    ↓
Chọn Service Package
    ↓
Chọn ngày / giờ / địa điểm
    ↓
Hệ thống kiểm tra trùng lịch
    ↓
PENDING
    ↓
Photographer ACCEPT / REJECT
    ↓
ACCEPTED
    ↓
Ngày chụp → IN_PROGRESS
    ↓
Hoàn thành → COMPLETED
    ↓
Customer review
```

### 5.3 Chat

```text
Booking tồn tại
    ↓
Customer / Photographer mở chat
    ↓
WebSocket kết nối
    ↓
Gửi message realtime
    ↓
Lưu SQL Server
```

---

## 6. Status chuẩn

### `users.status`
- `ACTIVE`
- `LOCKED`

### `photographer_profiles.approval_status`
- `PENDING`
- `APPROVED`
- `REJECTED`

### `service_packages.status`
- `ACTIVE`
- `INACTIVE`

### `bookings.status`
- `PENDING`
- `ACCEPTED`
- `REJECTED`
- `CANCELLED`
- `IN_PROGRESS`
- `COMPLETED`

### `portfolio_items.status`
- `ACTIVE`
- `HIDDEN`

### `reviews.status`
- `VISIBLE`
- `HIDDEN`

### `deposits.status`
- `PENDING`
- `PROCESSING`
- `PAID`
- `FAILED`
- `CANCELLED`
- Historical states retained: `REFUNDED`, `FORFEITED`

---

## 7. Quy tắc nghiệp vụ

### Booking
1. Customer không được booking chính mình.
2. Photographer phải `APPROVED`.
3. Service package phải `ACTIVE`.
4. Không được booking thời điểm trong quá khứ.
5. Không tạo booking nếu photographer đã có booking `ACCEPTED` hoặc `IN_PROGRESS` bị trùng thời gian.
6. Customer chỉ được cancel khi booking chưa `IN_PROGRESS`.
7. Photographer chỉ được accept/reject booking của chính mình.
8. Booking `COMPLETED` không đổi ngược trạng thái.

### Review
1. Booking phải `COMPLETED`.
2. Reviewer phải là customer của booking.
3. Một booking chỉ có tối đa một review.

### Portfolio
1. Chỉ photographer sở hữu profile được sửa/xóa.
2. Ảnh lưu Cloudinary; SQL Server chỉ lưu URL/public_id.

---

## 8. Yêu cầu phi chức năng

- Password không lưu plaintext.
- Secret không commit GitHub.
- Validate input ở server.
- Phân quyền server-side.
- Dùng transaction cho các thao tác cập nhật nhiều bảng quan trọng.
- UI responsive cơ bản.
- Search/booking trang chính phản hồi ổn trong môi trường demo.
- Git commit đều theo tiến độ thật.

---

## 9. Tiêu chí MVP hoàn thành

MVP được coi là hoàn thành khi có thể demo trọn luồng:

```text
Register customer
→ Login
→ Tìm photographer
→ Xem portfolio
→ Booking
→ Photographer login
→ Accept
→ Chat
→ Mark completed
→ Customer review
→ Admin xem dữ liệu
```
