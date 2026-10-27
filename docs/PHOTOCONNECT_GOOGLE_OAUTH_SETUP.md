# PhotoConnect V6 — Thiết lập Google OAuth nguyên bản

## Kiến trúc

PhotoConnect dùng `spring-boot-starter-oauth2-client` của Spring Boot 3.3 để thực hiện Authorization Code + OpenID Connect trực tiếp với Google. Supabase không còn là identity broker và không còn biến môi trường bắt buộc.

Luồng đăng nhập:

1. JSP `/login` hiển thị nút Google khi có đủ hai credential.
2. Nút gọi `GET /oauth2/authorization/google`.
3. Spring Security tạo `state`, lưu authorization request ở HTTP session và chuyển hướng tới Google.
4. Google trả về `GET /login/oauth2/code/google`.
5. Spring Security đổi authorization code, kiểm tra chữ ký ID token qua Google JWK, issuer, audience, thời hạn và nonce.
6. PhotoConnect yêu cầu `email_verified=true`, dùng claim `sub` làm khóa danh tính ổn định và ánh xạ vào SQL Server.
7. Ứng dụng phát hành JWT PhotoConnect trong cookie HttpOnly/SameSite=Lax hiện hữu và xóa Spring Security context. Access/refresh token của Google không được lưu.

Email/mật khẩu và Gmail OTP vẫn là luồng độc lập: đăng ký tạo tài khoản chờ xác minh, gửi OTP bằng `JavaMailSender`, sau đó đăng nhập bằng BCrypt và nhận cùng loại JWT PhotoConnect.

## Tạo OAuth Client trong Google Cloud

1. Mở Google Cloud Console và chọn hoặc tạo project.
2. Cấu hình OAuth consent screen. Với ứng dụng ở chế độ Testing, thêm đúng Google account dùng để thử vào danh sách Test users.
3. Tạo Credentials → OAuth client ID → Web application.
4. Authorized JavaScript origins cho máy phát triển:

   `http://localhost:8080`

5. Authorized redirect URIs cho máy phát triển:

   `http://localhost:8080/login/oauth2/code/google`

URI phải khớp tuyệt đối scheme, host, port và path. Môi trường HTTPS phải đăng ký origin và callback HTTPS riêng; không dùng callback Supabase cũ.

## Lưu credential một lần bằng Windows DPAPI

Tại project:

```powershell
cd E:\Web\PhotoConnect_Project_Docs_Solo
.\scripts\setup-once.ps1 -Section Google
```

Nhập Google Client ID và Client Secret. Secret được đọc bằng `SecureString`; file `.photoconnect-local/secrets.dpapi.json` chỉ chứa ciphertext gắn với Windows user hiện tại và đã bị Git ignore.

Kiểm tra không làm lộ giá trị:

```powershell
.\scripts\check-setup.ps1
```

- `Google OAuth: NOT CONFIGURED`: thiếu một hoặc cả hai giá trị; ứng dụng vẫn chạy và ẩn nút Google.
- `Google OAuth: CONFIGURED`: DPAPI giải mã được cả hai giá trị.
- `Google OIDC provider metadata: VERIFIED`: endpoint metadata của Google truy cập được. Trạng thái này không đồng nghĩa một tài khoản đã đăng nhập thành công.

Khởi động:

```powershell
.\scripts\start-photoconnect.ps1
```

Launcher chỉ đưa `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` vào environment của process con. Credential không được ghi vào `application.properties` hoặc log.

## Kiểm thử đăng nhập

1. Mở `http://localhost:8080/login`.
2. Kiểm tra nút **Đăng nhập bằng Google** xuất hiện.
3. Đăng nhập bằng test user đã được cho phép trong consent screen.
4. Xác nhận callback quay về PhotoConnect, cookie `PHOTOCONNECT_AUTH` là HttpOnly và URL không chứa JWT.
5. Tài khoản Google mới phải có role `CUSTOMER`.
6. Nếu email đã thuộc tài khoản mật khẩu nhưng chưa có mapping Google, hệ thống phải từ chối tự động hợp nhất và hiển thị thông báo tiếng Việt.
7. Tài khoản `INACTIVE`, `BANNED` hoặc `PENDING_VERIFICATION` không được đăng nhập qua mapping Google.

## Tắt Google OAuth an toàn

Không đặt hai biến Google trong process khởi động, hoặc cập nhật cấu hình DPAPI theo quy trình quản trị credential. Khi một trong hai giá trị thiếu, client registration không được tạo và nút Google bị ẩn; email/password và Gmail OTP vẫn hoạt động. Việc tắt không xóa user hay bản ghi `external_auth_identities`.

## Lỗi thường gặp

- `redirect_uri_mismatch`: callback trong Google Cloud không khớp chính xác `http://localhost:8080/login/oauth2/code/google`.
- Nút Google không hiện: chạy `check-setup.ps1`; cần đủ Client ID và Client Secret rồi khởi động lại.
- `access_denied`: người dùng hủy hoặc test user chưa được thêm khi consent screen ở chế độ Testing.
- Thông báo email đã tồn tại: đây là bảo vệ chống chiếm tài khoản; đăng nhập bằng mật khẩu hiện hữu. V6 không tự động link dựa trên email.
- Không thể xác thực identity: kiểm tra thời gian hệ thống, mạng tới Google và cấu hình OAuth client; không đưa token hoặc secret vào ticket/log.

## Dữ liệu SQL Server

`external_auth_identities` lưu `user_id`, provider `GOOGLE`, stable `provider_subject` (`sub`) và `created_at`. Không lưu Google access token hoặc refresh token. V030 bổ sung unique `(user_id, provider)`; unique `(provider, provider_subject)` từ V014 vẫn được giữ nguyên.
