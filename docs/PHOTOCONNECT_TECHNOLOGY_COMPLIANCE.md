# PhotoConnect V6 — Đối chiếu công nghệ học thuật

## Kết luận kiến trúc

PhotoConnect vẫn là ứng dụng Java/Spring Boot 3.3 đóng gói WAR, render server-side bằng JSP/JSTL + Bootstrap và SiteMesh. SQL Server là nguồn dữ liệu duy nhất; Hibernate chạy `validate`, Flyway quản lý schema. WebSocket/STOMP, JWT cookie và Cloudinary không bị thay thế. Không có React, Vue, Angular, Next.js hoặc Thymeleaf.

| Yêu cầu | Hiện trạng nguồn | Bằng chứng chính |
|---|---|---|
| Java + Spring Boot | Java 21 source level, Spring Boot 3.3.0 | `pom.xml` |
| Spring MVC | Controller + JSP view resolver | `application.properties`, `controller/` |
| JSP/JSTL + Bootstrap | Server-rendered JSP, JSTL escaping, Bootstrap UI | `src/main/webapp/WEB-INF/views/` |
| SiteMesh | Decorator filter/config hiện hữu | `SiteMeshConfig`, `sitemesh3.xml` |
| JPA/Hibernate | Spring Data JPA, schema validation | `entity/`, `repository/`, `spring.jpa.hibernate.ddl-auto=validate` |
| SQL Server | Microsoft JDBC, Flyway SQL Server | `mssql-jdbc`, `flyway-sqlserver` |
| WebSocket/STOMP | Spring WebSocket/STOMP và channel authorization | `WebSocketConfig`, `StompSecurityChannelInterceptor` |
| Spring Security + JWT | Native OAuth2 Client cho OIDC; JWT nội bộ cho request ứng dụng | `GoogleOAuthSecurityConfig`, `JwtSecurityConfig` |
| Cloudinary | Upload/delivery service hiện hữu | `CloudinaryConfig`, delivery services |
| Gmail OTP | JavaMailSender, hashed OTP, expiry/attempt/cooldown | `EmailOtpServiceImpl`, `EmailServiceImpl` |

## Kiểm kê thay đổi xác thực

### Đã triển khai và tiếp tục sử dụng

- Email/password với BCrypt.
- Đăng ký CUSTOMER, trạng thái `PENDING_VERIFICATION`, Gmail SMTP OTP năm phút, giới hạn năm lần thử và resend cooldown.
- JWT PhotoConnect ký bằng secret runtime, cookie HttpOnly/SameSite, role/status được tái kiểm tra từ SQL Server.
- Bảng SQL Server `external_auth_identities` với stable provider subject.
- Native Spring Security OAuth2 Client/OIDC tại `/oauth2/authorization/google` và `/login/oauth2/code/google`.

### Supabase-dependent trước V6

- `SupabaseGoogleAuthClient`, controller `/auth/google*`, PKCE exchange thủ công và ba biến `SUPABASE_*` chỉ phục vụ Google identity.
- Các thành phần runtime này đã được gỡ. Không có feature nghiệp vụ hoặc database nào phụ thuộc Supabase.
- Bản ghi identity lịch sử và user được bảo toàn; migration cũ không bị sửa hoặc xóa.

### Cấu hình tùy chọn

- `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` chỉ tạo client registration khi cả hai có giá trị.
- Khi thiếu credential, Spring Boot vẫn khởi động, email/password vẫn hoạt động và JSP ẩn nút Google.
- Script DPAPI cũ có thể còn ciphertext Supabase lịch sử, nhưng resolver chủ động loại các khóa đó và không export vào process ứng dụng.

## Ranh giới bảo mật OIDC

- Spring Security thực hiện authorization-code flow, state/nonce, đổi code và xác minh chữ ký ID token bằng Google JWK.
- Client registration cố định issuer `https://accounts.google.com`, audience là configured Client ID, callback chuẩn của Spring và scopes `openid profile email`.
- Success handler kiểm tra lại issuer, audience, expiry, subject và `email_verified=true` trước khi gọi service.
- Mapping vĩnh viễn dùng `(GOOGLE, sub)`, không dùng email làm khóa.
- Tài khoản mới luôn `CUSTOMER/ACTIVE`; password là chuỗi ngẫu nhiên không được tiết lộ, được BCrypt hash nên không có default password đã biết.
- Email trùng tài khoản chưa link bị từ chối bằng thông báo tiếng Việt; không auto-merge.
- Mapping đã tồn tại vẫn phải qua kiểm tra trạng thái local; role Google không được tin cậy.
- Google access/refresh tokens không được lưu qua `TransientOAuth2AuthorizedClientRepository`, không render ra JSP và không xuất hiện trong URL/log.
- Sau OIDC, ứng dụng phát hành JWT/cookie hiện hữu và xóa Spring `SecurityContext`, tránh hai session-auth độc lập.

## Flyway

- V014 giữ bảng identity và unique `(provider, provider_subject)`.
- V028/V029 được bảo toàn.
- V030 là migration forward-only, kiểm tra duplicate trước khi thêm unique `(user_id, provider)`; không xóa hoặc cập nhật dữ liệu lịch sử.
- `validate-on-migrate=true`, `clean-disabled=true`; không có `flyway.repair()` tự động.

## Phạm vi xác minh

Các unit/controller tests kiểm tra claims, role CUSTOMER, account status, duplicate email, JWT issuance, lỗi callback, CSRF và regression email/OTP. Xác minh issuer/audience/chữ ký ở tầng protocol vẫn dựa trên implementation được hỗ trợ của Spring Security; test không tự giả mạo signature verifier.

Đăng nhập Google thật chỉ được đánh dấu VERIFIED sau khi có credential và một test account được người dùng cho phép. Nếu chưa có, kết quả phải là BLOCKED chứ không suy diễn từ mock test.

## Kết quả xác minh độc lập V6 — 27/10/2026

- `mvn clean verify`: **914 test**, 882 pass, 0 failure, 0 error, 32 skipped; build WAR thành công. Các test bị skip trong lệnh mặc định đều bị khóa bởi biến môi trường SQL Server, không được tính là pass trong số 882.
- 32 test SQL Server bị skip mặc định đã được chạy riêng trên database dùng một lần: 31/31 test application/repository/registration pass; test V029 → V030/unique identity 1/1 pass. Các database tạm đã được xóa và database `PhotoConnect` không được dùng cho các lượt này.
- Startup từ WAR trên SQL Server tạm: có Google credential, `/login` trả 200 và `/oauth2/authorization/google` trả 302 tới `accounts.google.com`; không có credential, `/login` vẫn trả 200 và không render nút Google.
- 55 test bảo mật/xác thực mục tiêu pass, bao gồm issuer, audience, expiry, verified email, account status, role CUSTOMER, same-email takeover, callback failure, JWT, role revocation, CSRF và concurrency constraint.
- 52/52 PlantUML source pass syntax check. Năm export bị ảnh hưởng (`UC-00`, `UC-01`, `ARCH-01`, `ARCH-08`, `SITE-01`) đã được render lại; không còn route hoặc nhãn Supabase cũ.
- Gmail/DPAPI/SQL/Google configuration được checker xác nhận là configured; SQL connectivity và Google provider metadata reachable. Registration → OTP → activation → JWT login pass với mail capture trên SQL Server tạm. Một đăng nhập Google thật bằng test account được cho phép vẫn **BLOCKED**, nên không tuyên bố external Google login VERIFIED.

**Đánh giá:** READY WITH LIMITATIONS — implementation và integration cục bộ đạt yêu cầu; release production còn cần một lần Google consent/callback thật bằng tài khoản test được ủy quyền và xác minh cấu hình HTTPS/cookie tại môi trường triển khai.
