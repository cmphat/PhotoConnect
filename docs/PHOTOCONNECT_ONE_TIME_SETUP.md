# PhotoConnect V6 — Thiết lập một lần trên Windows 11

## Mục tiêu

Các script trong `scripts/` lưu cấu hình cục bộ theo từng project. Giá trị được mã hóa bằng Windows DPAPI và chỉ được giải mã trong ngữ cảnh tài khoản Windows đã tạo file. Không có mật khẩu, JWT key hoặc API secret nào được ghi vào `application.properties` hay Git.

Thư mục cục bộ `.photoconnect-local/` chứa ciphertext và thông tin PID. Toàn bộ thư mục này đã được thêm vào `.gitignore`.

## Yêu cầu

- Windows 11 và PowerShell.
- Java 21 trở lên. Launcher ưu tiên Java 21 nếu tìm thấy; Java mới hơn được dùng khi không có Java 21.
- Maven có lệnh `mvn`/`mvn.cmd` trong `PATH`.
- SQL Server đang chạy và đã có database `PhotoConnect`.
- SQL login có quyền đọc/ghi schema cần thiết để Flyway kiểm tra và áp dụng migration hợp lệ.

## Thiết lập lần đầu

Mở PowerShell tại thư mục project:

```powershell
cd E:\Web\PhotoConnect_Project_Docs_Solo
.\scripts\setup-once.ps1
```

Script luôn yêu cầu SQL Server nếu chưa cấu hình và tự sinh JWT signing key mạnh nếu chưa có. Gmail, Cloudinary và Google OAuth là tùy chọn. Password được nhập bằng `SecureString`, không hiển thị trên màn hình.

Kiểm tra sau khi thiết lập:

```powershell
.\scripts\check-setup.ps1
```

`CONFIGURED` nghĩa là ciphertext cần thiết tồn tại và giải mã được. `VERIFIED` nghĩa là script đã thực sự kiểm tra runtime, kết nối SQL, HTTP hoặc metadata OIDC. Script chẩn đoán không gửi email, không gọi API Cloudinary và không đăng nhập Google thay người dùng; Google OAuth chỉ được xác minh đầu-cuối sau một lần đăng nhập tương tác thành công.

## Gmail SMTP

Tạo App Password trong tài khoản Google có bật xác minh hai bước, sau đó chạy:

```powershell
.\scripts\setup-once.ps1 -Section Gmail
```

Nhập Gmail address và App Password khi được hỏi. Cấu hình dùng đúng các biến của ứng dụng:

- `MAIL_HOST` (mặc định `smtp.gmail.com`)
- `MAIL_PORT` (mặc định `587`)
- `MAIL_USERNAME`
- `MAIL_APP_PASSWORD`
- `MAIL_FROM`
- `MAIL_FROM_NAME`
- `MAIL_CAPTURE_MODE=false`

Launcher luôn đặt capture mode thành `false` khi cả username và App Password đã được cấu hình. Nếu chưa cấu hình Gmail, launcher đặt capture mode thành `true` để phát triển cục bộ. `check-setup.ps1` chỉ xác nhận cấu hình; email chỉ được xác minh hoàn toàn khi thực hiện một luồng đăng ký được phép tới người nhận thử nghiệm.

## Cloudinary

```powershell
.\scripts\setup-once.ps1 -Section Cloudinary
```

Nhập Cloud name, API key và API secret. Cả ba trường phải đầy đủ; launcher từ chối chạy cấu hình Cloudinary một phần.

## Google OAuth nguyên bản

Tạo OAuth 2.0 Client loại Web application trong Google Cloud, khai báo origin `http://localhost:8080` và redirect URI `http://localhost:8080/login/oauth2/code/google`, sau đó chạy:

```powershell
.\scripts\setup-once.ps1 -Section Google
```

Script lưu:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Client secret được nhập bằng `SecureString`; cả hai giá trị được mã hóa bằng DPAPI. Supabase không còn nằm trong luồng runtime. Nếu bỏ trống phần Google, ứng dụng vẫn khởi động và nút Google được ẩn.

## Chạy ứng dụng

Mỗi lần mở PowerShell mới, chỉ cần:

```powershell
cd E:\Web\PhotoConnect_Project_Docs_Solo
.\scripts\start-photoconnect.ps1
```

Launcher thực hiện lần lượt:

1. Giải mã cấu hình bằng DPAPI của user hiện tại.
2. Kiểm tra Java, Maven, kết nối SQL Server và cổng 8080.
3. Đặt environment variables chỉ trong process launcher và process Maven con.
4. Chạy `mvn spring-boot:run` với stdout/stderr bình thường.
5. Chỉ hiển thị `http://localhost:8080/login` sau khi endpoint thực sự trả HTTP 200.

Script không gọi Flyway repair, không clean database và không thay đổi cấu hình migration.

## Dừng ứng dụng

Mở PowerShell khác tại project và chạy:

```powershell
.\scripts\stop-photoconnect.ps1
```

Script chỉ dừng cây process có PID và thời điểm khởi chạy khớp metadata do launcher tạo. Nó không dừng SQL Server và không tìm/kill toàn bộ process Java.

## Cập nhật một credential

Chạy đúng section cần thay đổi:

```powershell
.\scripts\setup-once.ps1 -Section SqlServer
.\scripts\setup-once.ps1 -Section Jwt
.\scripts\setup-once.ps1 -Section Gmail
.\scripts\setup-once.ps1 -Section Cloudinary
.\scripts\setup-once.ps1 -Section Google
```

Nếu section đã tồn tại, script hỏi xác nhận trước khi ghi đè. Các section khác được giữ nguyên.

## Khi đổi tài khoản Windows hoặc cài lại Windows

DPAPI ciphertext không thể chuyển sang tài khoản Windows khác. Đây là hành vi bảo mật mong muốn.

1. Không sao chép file `secrets.dpapi.json` để mong đợi giải mã trên user mới.
2. Xóa hoặc đổi tên thư mục `.photoconnect-local` bằng chính tài khoản sở hữu nếu cần lưu bản cũ.
3. Đăng nhập tài khoản Windows mới và chạy lại `setup-once.ps1`.
4. Nhập lại credential từ nguồn quản lý bí mật chính thức; không cố giải mã thủ công ciphertext cũ.

## Khắc phục sự cố

### Không giải mã được cấu hình

Đảm bảo đang dùng đúng Windows account. Nếu SID đã thay đổi, chạy lại setup và tạo cấu hình mới.

### SQL Server FAILED

- Kiểm tra SQL Server service và TCP port 1433.
- Kiểm tra `DB_URL`, database name và SQL Authentication.
- Cập nhật riêng SQL bằng `setup-once.ps1 -Section SqlServer`.

### Cổng 8080 đang được dùng

Nếu ứng dụng do launcher mở, chạy `stop-photoconnect.ps1`. Nếu không, tìm ứng dụng sở hữu cổng; launcher sẽ không tự kill process lạ.

### Gmail chỉ ở capture mode

Chạy `setup-once.ps1 -Section Gmail`, nhập đủ username/App Password rồi chạy lại ứng dụng. Dùng `check-setup.ps1` để xác nhận `Gmail capture mode: CONFIGURED - disabled for real SMTP`.

### Maven thoát trước khi server sẵn sàng

Đọc lỗi Maven/Spring Boot được in trực tiếp trên terminal. URL không được hiển thị như thành công nếu process thoát hoặc endpoint chưa trả HTTP 200.

### Kiểm tra Git

```powershell
git check-ignore -v .photoconnect-local/secrets.dpapi.json
git status --short
```

File bí mật phải được Git ignore và không xuất hiện trong danh sách untracked.
