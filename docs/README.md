# PhotoConnect

**PhotoConnect** là nền tảng web kết nối **khách hàng** với **nhiếp ảnh gia**, hỗ trợ xem hồ sơ/portfolio, tìm kiếm theo nhu cầu, đặt lịch chụp, quản lý booking, chat realtime và đánh giá sau buổi chụp.

> Đây là đồ án cá nhân. Toàn bộ phạm vi được thiết kế cho **1 sinh viên thực hiện trong 8 tuần**.

## Stack chốt

- Java 26
- Spring Boot / Spring MVC
- JSP / JSTL
- Bootstrap
- Spring Data JPA / Hibernate
- SQL Server + SQL Server Authentication
- Spring Security + JWT
- WebSocket / STOMP
- Cloudinary
- Sitemesh
- Maven
- Apache Tomcat 10.1
- Git / GitHub

## Vai trò

- `CUSTOMER` — khách hàng
- `PHOTOGRAPHER` — nhiếp ảnh gia
- `ADMIN` — quản trị viên

## Phạm vi MVP

### Bắt buộc
- Đăng ký / đăng nhập / phân quyền
- Hồ sơ photographer
- Admin duyệt photographer
- Portfolio + Cloudinary
- Gói dịch vụ
- Search / filter photographer
- Booking + quản lý trạng thái
- Kiểm tra trùng lịch cơ bản
- Chat realtime
- Review
- Admin dashboard cơ bản

### Bonus nếu còn thời gian
- Voucher
- Cọc mô phỏng
- Notification nâng cao
- AI nhận xét ảnh
- Gợi ý photographer

## Tài liệu

| File | Nội dung |
|---|---|
| `SPEC.md` | Đặc tả nghiệp vụ gốc |
| `SETUP.md` | Cài môi trường và chạy project |
| `ERD.md` | Thiết kế database |
| `API_CONTRACT.md` | MVC routes + API contract |
| `ERROR_CODES.md` | Mã lỗi chuẩn hóa |
| `REALTIME_EVENTS.md` | WebSocket events |
| `SECURITY.md` | Authentication, authorization, secret |
| `CODING_CONVENTION.md` | Quy ước code/Git |
| `PLAN_BE.md` | Kế hoạch backend |
| `PLAN_FE.md` | Kế hoạch frontend |
| `ROADMAP.md` | Roadmap 8 tuần |
| `SOLO_WORKFLOW.md` | Quy trình làm đồ án một mình |

## Nguyên tắc

`SPEC.md` là **nguồn tham chiếu nghiệp vụ gốc**.  
Các file `ERD.md`, `API_CONTRACT.md`, `REALTIME_EVENTS.md`, `ERROR_CODES.md` phải thống nhất tên role, status và entity với `SPEC.md`.
