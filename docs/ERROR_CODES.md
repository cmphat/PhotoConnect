# ERROR_CODES.md — Mã Lỗi Chuẩn

Format:

`DOMAIN_XXX_DESCRIPTION`

---

## AUTH

| Code | Meaning |
|---|---|
| `AUTH_001_INVALID_CREDENTIALS` | Sai email/password |
| `AUTH_002_EMAIL_ALREADY_EXISTS` | Email đã tồn tại |
| `AUTH_003_TOKEN_INVALID` | JWT không hợp lệ |
| `AUTH_004_TOKEN_EXPIRED` | JWT hết hạn |
| `AUTH_005_ACCESS_DENIED` | Không đủ quyền |

## USER

| Code | Meaning |
|---|---|
| `USER_001_NOT_FOUND` | Không tìm thấy user |
| `USER_002_ACCOUNT_LOCKED` | Tài khoản bị khóa |
| `USER_003_INVALID_ROLE` | Role không hợp lệ |

## PHOTOGRAPHER

| Code | Meaning |
|---|---|
| `PHOTOGRAPHER_001_NOT_FOUND` | Không tìm thấy photographer |
| `PHOTOGRAPHER_002_NOT_APPROVED` | Chưa được admin duyệt |
| `PHOTOGRAPHER_003_PROFILE_EXISTS` | Profile đã tồn tại |
| `PHOTOGRAPHER_004_NOT_OWNER` | Không sở hữu tài nguyên |

## PORTFOLIO

| Code | Meaning |
|---|---|
| `PORTFOLIO_001_FILE_REQUIRED` | Thiếu ảnh |
| `PORTFOLIO_002_INVALID_FILE_TYPE` | Sai loại file |
| `PORTFOLIO_003_UPLOAD_FAILED` | Cloudinary upload lỗi |
| `PORTFOLIO_004_NOT_FOUND` | Không tìm thấy portfolio item |

## PACKAGE

| Code | Meaning |
|---|---|
| `PACKAGE_001_NOT_FOUND` | Không tìm thấy gói |
| `PACKAGE_002_INACTIVE` | Gói không hoạt động |
| `PACKAGE_003_INVALID_PRICE` | Giá không hợp lệ |

## BOOKING

| Code | Meaning |
|---|---|
| `BOOKING_001_NOT_FOUND` | Không tìm thấy booking |
| `BOOKING_002_TIME_CONFLICT` | Photographer bị trùng lịch |
| `BOOKING_003_INVALID_STATUS` | Chuyển trạng thái không hợp lệ |
| `BOOKING_004_PAST_TIME` | Thời gian booking ở quá khứ |
| `BOOKING_005_NOT_OWNER` | Không thuộc user hiện tại |
| `BOOKING_006_CANNOT_CANCEL` | Không thể hủy |
| `BOOKING_007_SELF_BOOKING` | Photographer tự booking chính mình |

## REVIEW

| Code | Meaning |
|---|---|
| `REVIEW_001_BOOKING_NOT_COMPLETED` | Booking chưa completed |
| `REVIEW_002_ALREADY_EXISTS` | Booking đã review |
| `REVIEW_003_INVALID_RATING` | Rating ngoài 1..5 |
| `REVIEW_004_NOT_OWNER` | Customer không sở hữu booking |

## CHAT

| Code | Meaning |
|---|---|
| `CHAT_001_BOOKING_REQUIRED` | Chat phải gắn booking |
| `CHAT_002_ACCESS_DENIED` | Không thuộc booking |
| `CHAT_003_MESSAGE_EMPTY` | Message rỗng |
| `CHAT_004_MESSAGE_TOO_LONG` | Message vượt giới hạn |

## ADMIN

| Code | Meaning |
|---|---|
| `ADMIN_001_CANNOT_MODIFY_SELF` | Admin không được khóa chính mình |
| `ADMIN_002_INVALID_APPROVAL_STATUS` | Trạng thái duyệt sai |

## SYSTEM

| Code | Meaning |
|---|---|
| `SYSTEM_001_DATABASE_ERROR` | Database lỗi |
| `SYSTEM_002_INTERNAL_ERROR` | Lỗi nội bộ |
| `SYSTEM_003_VALIDATION_ERROR` | Dữ liệu không hợp lệ |
