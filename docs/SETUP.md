# SETUP.md — Cài Đặt Và Chạy Dự Án

## 1. Phần mềm cần có

- JDK 26
- Spring Tools for Eclipse 5.x
- Maven 3.9.x
- Apache Tomcat 10.1.x
- SQL Server Developer
- SQL Server Management Studio (SSMS)
- Git
- Tài khoản Cloudinary

---

## 2. Java

Kiểm tra:

```powershell
java -version
javac -version
```

Kỳ vọng Java 26.

Trong Spring Tools:

```text
Window
→ Preferences
→ Java
→ Installed JREs
```

Project:

```text
Properties
→ Java Build Path
→ JRE System Library [JavaSE-26]
```

Compiler:

```text
Properties
→ Java Compiler
→ 26
```

---

## 3. Maven

Kiểm tra:

```powershell
mvn -version
```

Local repository hiện dùng:

```text
E:\.m2\repository
```

User settings khuyến nghị:

```text
C:\Users\ADMIN\.m2\settings.xml
```

Ví dụ:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <localRepository>E:\.m2\repository</localRepository>
</settings>
```

---

## 4. Tomcat

Đường dẫn hiện tại:

```text
E:\Web\Tool\apache-tomcat-10.1.44
```

Spring Tools:

```text
Window
→ Preferences
→ Server
→ Runtime Environments
```

Chọn Apache Tomcat v10.1.

Nếu chạy ngoài terminal:

```powershell
cd "E:\Web\Tool\apache-tomcat-10.1.44\bin"
.\catalina.bat run
```

Nếu port 8080 bị chiếm:

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
Select-Object LocalAddress,LocalPort,State,OwningProcess
```

---

## 5. SQL Server

Cài:
- SQL Server Developer
- SSMS

Bật **Mixed Mode / SQL Server Authentication**.

Tạo database:

```sql
CREATE DATABASE PhotoConnectDB;
GO
```

Tạo login riêng:

```sql
CREATE LOGIN photoconnect
WITH PASSWORD = 'CHANGE_ME_STRONG_PASSWORD';
GO

USE PhotoConnectDB;
GO

CREATE USER photoconnect FOR LOGIN photoconnect;
GO

ALTER ROLE db_datareader ADD MEMBER photoconnect;
ALTER ROLE db_datawriter ADD MEMBER photoconnect;
GO
```

Trong môi trường dev có thể cấp thêm quyền schema cần thiết nếu Hibernate tạo bảng.

---

## 6. Cấu hình Spring

Khuyến nghị để secret qua environment variable.

`application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=PhotoConnectDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=${DB_USERNAME:photoconnect}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.servlet.context-path=/photoconnect
```

Cloudinary:

```properties
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

JWT:

```properties
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=86400000
```

---

## 7. Environment variables

PowerShell ví dụ:

```powershell
$env:DB_USERNAME="photoconnect"
$env:DB_PASSWORD="..."
$env:JWT_SECRET="..."
$env:CLOUDINARY_CLOUD_NAME="..."
$env:CLOUDINARY_API_KEY="..."
$env:CLOUDINARY_API_SECRET="..."
```

Không commit giá trị thật.

---

## 8. GitHub

Repository:

```text
https://github.com/cmphat/PhotoConnect.git
```

Lần đầu:

```powershell
git init
git add .
git commit -m "Initial project setup"
git branch -M main
git remote add origin https://github.com/cmphat/PhotoConnect.git
git push -u origin main
```

Mỗi lần cập nhật:

```powershell
git pull
git status
git add .
git commit -m "Mô tả thay đổi"
git push
```

---

## 9. Chạy dự án

Quy trình mỗi buổi:

```text
1. Kiểm tra SQL Server đang chạy
2. Mở Spring Tools
3. Pull Git
4. Start Tomcat
5. Code
6. Test
7. Commit
8. Push
```

---

## 10. Kiểm tra setup thành công

- JavaSE-26
- Tomcat `Started, Synchronized`
- SQL Server kết nối được
- `mvn clean package` chạy thành công
- Trang test JSP mở được
- Git push thành công
