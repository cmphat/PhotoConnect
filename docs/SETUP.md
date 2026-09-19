# Setup

The maintained setup procedure is [final/SETUP_GUIDE.md](final/SETUP_GUIDE.md).

Key facts:

- Maven compiles for Java 21 and produces `target/photoconnect.war`.
- The database is Microsoft SQL Server database `PhotoConnect` by default.
- Supply `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` through the environment.
- Supply Cloudinary variables only for portfolio upload/delete.
- The optional demo seed is opt-in, idempotent, and non-destructive.
- Stop PhotoConnect/Spring Boot/DevTools before `mvn clean` on Windows.
- No JWT secret, Node toolchain, or frontend framework setup is required.
