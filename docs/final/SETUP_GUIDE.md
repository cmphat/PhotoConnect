# Clean Windows Setup Guide

## 1. Install prerequisites

- JDK 21 or newer (`java -version`, `javac -version`)
- Maven 3.9+ (`mvn -version`)
- Microsoft SQL Server 2019+ and SSMS or `sqlcmd`
- Git

The Maven project compiles for Java 21 and packages a WAR. A separate Node or frontend toolchain is not used.

## 2. Create the database

Connect with an authorized SQL Server account and create the local database if it does not exist:

```sql
CREATE DATABASE PhotoConnect;
GO
```

Use a dedicated development login where possible. It needs normal data access and, while `spring.jpa.hibernate.ddl-auto=update` is enabled, permission to create/alter the application schema. Do not place passwords in the repository.

## 3. Configure the PowerShell session

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=PhotoConnect;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="DB_USERNAME"
$env:DB_PASSWORD="DB_PASSWORD"
$env:WEBSOCKET_ALLOWED_ORIGINS="http://localhost:8080"
```

Only if portfolio upload/delete will be used:

```powershell
$env:CLOUDINARY_CLOUD_NAME="CLOUDINARY_CLOUD_NAME"
$env:CLOUDINARY_API_KEY="CLOUDINARY_API_KEY"
$env:CLOUDINARY_API_SECRET="CLOUDINARY_API_SECRET"
```

`PAYMENT_SIMULATION_ENABLED` defaults to `true` for the disclosed local demo. Set it to `false` in an environment where checkout must be unavailable.

## 4. Prepare the schema

On a new development database, start once with the current `ddl-auto=update` configuration to create the mapped base tables. For an older database, review and apply the forward-only scripts in order when their columns are absent:

```text
docs/development/migrations/V008__add_review_status.sql
docs/development/migrations/V010__professional_demo_payment.sql
```

`V009__demo_seed_data.sql` is optional data, not a schema migration. Back up valuable data before manual database work. Do not edit an already-applied migration.

## 5. Optional demo data

Full scripted dataset in SSMS:

```text
Open docs/development/seed/V009__demo_seed_data.sql
Confirm the selected database is PhotoConnect
Execute once; rerunning is guarded
```

Or create only guarded demo accounts/profiles through the application:

```powershell
$env:DEMO_PASSWORD="CHOOSE_A_PRIVATE_DEMO_PASSWORD"
mvn spring-boot:run "-Dspring-boot.run.profiles=demo-seed" "-Dspring-boot.run.arguments=--photoconnect.demo.seed-enabled=true"
```

The Java seed requires both the `demo-seed` profile and `photoconnect.demo.seed-enabled=true`. Both seed paths are intended to be idempotent and non-destructive. Existing matching accounts are preserved, so actual database totals can exceed the intended seed set.

## 6. Test, build, and run

```powershell
mvn test
mvn package
mvn spring-boot:run
```

Browse to `http://localhost:8080/`. The packaged WAR is `target/photoconnect.war`.

### Important Windows clean rule

Never run `mvn clean` while the PhotoConnect Spring Boot/DevTools application is active. Stop the app and verify port 8080/process state first, because Windows can lock files under `target/`.

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
    Select-Object LocalAddress, LocalPort, State, OwningProcess
```

After the application is stopped:

```powershell
mvn clean package
```

## 7. Common checks

- **Login/database failure:** confirm SQL Server is running and the three `DB_*` values target the intended database.
- **WebSocket fallback shown:** confirm the external browser libraries are reachable and `WEBSOCKET_ALLOWED_ORIGINS` exactly includes the browser origin. Chat can continue through REST fallback.
- **Portfolio upload unavailable:** supply all three Cloudinary variables; do not invent or commit credentials.
- **Port 8080 occupied:** stop the owning process or intentionally configure a different server port and matching WebSocket origin.
- **Integration tests skipped:** provide a reachable SQL Server test environment only if you intend to run those environment-gated tests.
