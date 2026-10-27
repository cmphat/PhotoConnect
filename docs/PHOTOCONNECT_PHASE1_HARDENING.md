# Photo Connect — Phase 1 hardening (2026-10-28)

## Scope and baseline
- Source: `v5-master-development` at `2d5178e9bc5018448afdbbdf0118b7ded11f90f8`.
- Changes only on `refactor/photo-connect-phase1-hardening`; original V5 and main remain unchanged.
- Java 21, Spring Boot 3.3, executable WAR with JSP/JSTL/SiteMesh, SQL Server/Flyway, Cloudinary, STOMP WebSocket.

## Security fixes
1. JWT refuses an additional previously published key via SHA-256 fingerprint, alongside existing denylist. Never configure repository-published signing keys; rotate secrets in the real environment.
2. Demo seeding requires `demo-seed` without `prod`, explicit `photoconnect.demo.seed-enabled=true` and explicit `DEMO_PASSWORD`. The previously published password is rejected.
3. Flyway `baseline-on-migrate` defaults to **false**. Upgrading an *existing unversioned* database now requires a reviewed migration/baseline plan. No database migration was executed and no database data modified.

## Outstanding verification
- No fresh `mvn clean verify` / SQL Server integration test has been executed by this change alone. Historical audit results are not current acceptance evidence.
- Run: `mvn -B clean verify` with Java 21. Include disposable SQL Server clone to run DB integration tests (use environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- Check `CloudinaryStorageServiceImpl.generateAuthorizedUrl` against a disposable authenticated asset. The historical audit reports a missing auth-token key in Cloudinary 2.0.0; do **not** expose private assets or silently revert to public URLs.
- Inventory legacy delivery assets for public `/upload/` URLs before release; verify no anonymous access. Never delete or migrate actual customer media without approval.
- Verify authenticated STOMP chat with two actual clients; verify booking concurrency with independent SQL transactions.
- Check `JWT_SECRET`, OAuth, Cloudinary, SMTP and payment provider configuration through secret storage (names only, never print values).

## Deployment note
A Spring Boot WAR + JSP and SQL Server + STOMP server is **not** a static Vercel site. A static UI preview, if deployed separately, cannot be called a functioning backend. Real deployments must use an explicitly validated compatible Java/container runtime and reachable database, or a suitable Java host. Keep payment mode in DEMO.

## Phase 1 acceptance checklist
- [x] Isolated working branch
- [x] JWT regression guard and unit assertion
- [x] No default demo password and published demo password rejected
- [x] Production profile excludes demo seed; Flyway default fail-closed
- [ ] Fresh Java 21 Maven verify on exact commit
- [ ] Disposable SQL Server migration validation on fresh and upgraded DBs
- [ ] Cloudinary private delivery authenticated integration
- [ ] Real HTTP/auth/STOMP smoke test
- [ ] Verified live deployment preview (not static mock)
