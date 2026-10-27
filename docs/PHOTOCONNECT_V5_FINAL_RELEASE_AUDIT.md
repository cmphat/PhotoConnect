# PhotoConnect V5 — Final Independent Release Acceptance Audit

**Audit date:** 2026-10-26 (Asia/Saigon)  
**Audited branch / HEAD:** `v5-master-development` / `82640c4cf588a82bf9eb8f9b36c80aa4aa37b3c1`  
**Audited subject:** the complete working tree, including uncommitted and untracked V5 Manager changes  
**Release decision:** **NOT READY**

## Executive decision

The reported `902/902` result is reproducible, and all 29 SQL-gated tests did execute against an independently created disposable SQL Server database. A fresh empty database migrated through the 19 available scripts from V001 to V027 and passed Hibernate schema validation.

That result is not sufficient for release acceptance. Four release-blocking conditions remain:

1. startup calls `flyway.repair()` before every migration, and an already-versioned V013 script has been edited; an isolated checksum-tampering test proved startup silently rewrites drift instead of rejecting it;
2. private Cloudinary preview/download URL generation fails in the real Cloudinary SDK path because the constructed `AuthToken` has no token key; the unit tests mock this path;
3. the previously published JWT signing key is not in the current denylist and the packaged application starts successfully when configured with that known key;
4. the claimed release is not reproducible from Git: the Manager implementation and V027 are untracked, with 44 modified tracked files and 209 untracked files overall.

The end-to-end business state journey worked over real HTTP through booking, deposit, chat, revision, redelivery, completion and review. A real private-media upload/delivery could not complete without authorized Cloudinary credentials, and the concrete URL-generation defect would block preview/download even when ordinary API credentials are supplied.

## Scope, isolation and evidence rules

- Application source, migrations, Git history and the development database were not modified by this audit.
- Tests ran from the isolated copy `E:\Web\_photoconnect_audit_20261026_020717`.
- SQL Server database `PCV5Audit_20261026_0209` and login `PCV5AuditLogin_20261026_0209` were created exclusively for this audit.
- Mail capture mode and simulated payment were enabled. No public deployment was made.
- No customer Cloudinary asset was changed or deleted.
- The only intended repository write is this report.
- Cleanup was verified: the disposable database and login both returned zero existence counts, and the isolated filesystem copy was removed. Antigravity's existing Java/Spring processes were not stopped or altered.
- “Executed” below means observed during this audit. Claims in existing project documents are not counted as execution evidence.

## Release-blocking findings

### RA-01 — HIGH — Automatic Flyway repair masks migration drift

**Location:**

- `src/main/java/com/photoconnect/config/FlywayConfig.java:10-29`
- `src/main/resources/application.properties:23-31`
- `src/main/resources/db/migration/V013__create_saved_photographers.sql:22-25`
- `src/main/resources/db/migration/V027__add_manager_operations.sql:79-91`

**Root cause:** `FlywayConfig` unconditionally executes `flyway.repair()` before `flyway.migrate()` at every application startup. At the same time, V013 has been changed in the working tree from `ON DELETE CASCADE` to `ON DELETE NO ACTION`. Repair updates schema-history checksums and can delete failed entries, so validation no longer protects an environment from an unexpected change to an applied migration.

**Independent reproduction:**

1. Migrate a fresh disposable SQL Server database to V027.
2. Record V013 checksum `1712738511`.
3. Change only the disposable schema-history checksum to `1712738512`.
4. Start the Spring context through `DatabaseConnectionIntegrationTests`.
5. Observe `Repairing Flyway schema history...`, a successful startup, and checksum rewritten to `1712738511`.

**Actual:** startup silently accepts and rewrites checksum drift.

**Expected:** normal startup runs Flyway validation/migration and fails closed on checksum mismatch. Repair is an explicit, reviewed operator action after backup and root-cause analysis.

**Recommended release action:** remove the automatic migration strategy and use standard validate-on-migrate behavior. Do not edit an applied V013 in place. First inventory every deployed schema-history table. If V013 was never released, coordinate the one-time pre-release migration lineage explicitly. If it was released, restore its immutable artifact and introduce a documented forward/baseline strategy for fresh installs without rewriting deployed history. Never use startup repair as the compatibility mechanism.

**Verification:** **FAILED**. Fresh installation succeeds; safe upgrade/checksum behavior does not.

### RA-02 — HIGH — Real private-media URL generation fails; 300-second claim is unverified

**Location:**

- `src/main/java/com/photoconnect/config/CloudinaryConfig.java:32-54`
- `src/main/java/com/photoconnect/service/CloudinaryStorageServiceImpl.java:89-114`
- `src/main/java/com/photoconnect/service/DeliveryServiceImpl.java:319-364`
- `src/test/java/com/photoconnect/service/DeliveryServiceTest.java:364-411,568-589`

**Root cause:** when an API secret is configured, `generateAuthorizedUrl` creates `new AuthToken().duration(...)` without an auth-token key. `CloudinaryConfig` supplies only cloud name, API key and API secret. The SDK attempts to digest with a null token key. Tests mock `CloudinaryStorageService.generateAuthorizedUrl` and therefore never execute the concrete SDK URL builder.

**Independent reproduction:** the Cloudinary 2.0.0 SDK bundled in the built WAR was instantiated with nonblank dummy cloud name/API key/API secret. Calling the same `signed(true)` plus `new AuthToken().duration(300)` sequence threw `NullPointerException` in `AuthToken.digest`. During the HTTP journey, preview returned to the gallery because URL generation returned null. The application log recorded `Unable to generate authorized preview link`.

**Actual:** the production method catches the SDK error and returns null; customer/admin preview and download cannot produce a usable URL. No real 300-second delivery was demonstrated.

**Expected:** the configured Cloudinary access-control mechanism generates a usable, expiring URL for an authenticated asset, and an integration test verifies both successful authorized retrieval and expiry/anonymous rejection.

**Recommended release action:** configure and use the correct Cloudinary auth-token key or use Cloudinary’s supported authenticated-delivery signing flow without claiming an expiry it does not enforce. Add a concrete SDK integration test; do not mock `Url.generate`. Verify the resulting URL against a disposable authenticated asset before release.

**Verification:** **FAILED**.

### RA-03 — HIGH — Previously leaked JWT signing key is still accepted

**Location:**

- active denylist: `src/main/java/com/photoconnect/security/JwtTokenService.java:20-23,33-53`
- startup enforcement: `src/main/java/com/photoconnect/config/JwtSecurityConfig.java:26-35`
- published old key: `docs/PHOTOCONNECT_V4_RELEASE_BLOCKERS.md:38`
- additional historical analysis: `docs/PHOTOCONNECT_V4_FINAL_INDEPENDENT_RELEASE_ACCEPTANCE.md:59-67`

**Root cause:** the current denylist contains two known strings, but not the exact previously committed fallback `dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ycGhvdG9jb25uZWN0and0YXV0aGVudGljYXRpb24=` documented in the repository.

**Independent reproduction:**

- with `JWT_SECRET` absent, the packaged WAR exited with code 1 and a critical configuration error — correct fail-closed behavior;
- with the published old key as `JWT_SECRET`, the packaged WAR started successfully on port 18082 — incorrect known-key acceptance.

**Actual:** no JWT fallback is active, but an operator can configure a repository-known compromised key and obtain a functioning signer.

**Expected:** every previously shipped/published signing key is rejected, and production obtains a unique high-entropy secret from an external secret store.

**Recommended release action:** add the exact historical key to the denylist and regression tests; rotate any environment that ever used it; avoid reproducing secret values in public-facing release documentation beyond a fingerprint/redacted form.

**Verification:** **FAILED**.

### RA-04 — HIGH — Known privileged demo password ships in runtime configuration and release documentation

**Location:**

- `src/main/resources/application-demo-seed.properties:1-4`
- `src/main/java/com/photoconnect/config/DemoDataSeeder.java:23-29,67-85`
- `docs/PHOTOCONNECT_V5_RELEASE_CANDIDATE.md:79-82`
- `docs/development/TASK-028-development-demo-data.md:25`

**Root cause:** the demo profile defaults `DEMO_PASSWORD` to `PhotoConnectDemo!2026`, and the release-candidate document publishes that password for Customer, Photographer, Manager and Admin accounts.

**Actual:** seeding requires both the `demo-seed` profile and `photoconnect.demo.seed-enabled=true`, which is a useful guard. If those are enabled without overriding the password, privileged known credentials are created.

**Expected:** enabling seed mode requires an explicitly supplied non-default password, preferably with no Admin/Manager seed in a production artifact.

**Recommended release action:** remove the password fallback so seed startup fails when `DEMO_PASSWORD` is absent; keep demo credentials out of public release documentation; add a production-profile guard that refuses seed mode.

**Verification:** **FAILED**. No live database/Cloudinary/SMTP credentials were found in production properties, but known demo credentials are shipped.

### RA-05 — HIGH — Legacy public Cloudinary delivery exposure has no enforced migration

**Location:**

- `src/main/java/com/photoconnect/entity/DeliveredPhoto.java:71-96`
- `src/main/java/com/photoconnect/service/DeliveryServiceImpl.java:319-388`
- `src/main/java/com/photoconnect/service/CloudinaryStorageServiceImpl.java:89-133`
- documentation acknowledgement: `docs/PHOTOCONNECT_V5_DIAGRAM_INDEX.md:117-122`

**Root cause:** a stored URL containing `/upload/` is classified as delivery type `upload`; URL generation preserves that public delivery type. A signed URL to a public `upload` asset does not make the original public URL private. A one-photo migration method exists, but no controller, startup job, migration command or inventory workflow invokes it.

**Actual:** new upload code requests `type=authenticated` and fails without falling back to public storage, which was observed: the upload returned to the delivery studio and inserted zero photo rows. Existing public assets remain public until separately migrated.

**Expected:** all delivery assets are inventoried and converted to authenticated delivery, old public URLs are invalidated, and the application refuses to issue delivery access for a legacy `upload` record until migration succeeds.

**Recommended release action:** run an authorized, auditable inventory/migration outside request handling; verify each asset’s new delivery type and old URL behavior; retain rollback metadata. Do not delete assets without owner authorization.

**Verification:** **PARTIALLY VERIFIED / EXTERNAL CHECK BLOCKED**. Source behavior is confirmed. No authorized inventory of real legacy customer asset IDs or Cloudinary credentials was available, so anonymous access to actual customer assets was not probed.

### RA-06 — HIGH — Release changes are not safely checkpointed in Git

**Location:** repository-wide Git state.

**Evidence:**

- branch `v5-master-development`, HEAD `82640c4cf588a82bf9eb8f9b36c80aa4aa37b3c1`, committed 2026-10-25 02:59:34 +07:00;
- 44 modified tracked files and 209 untracked files;
- tracked diff alone: 683 insertions and 124 deletions across 44 files;
- `ManagerController.java` and `V027__add_manager_operations.sql` do not exist at HEAD;
- the branch has no upstream shown by `git branch -vv`;
- `git diff --check` reports trailing whitespace in the release-candidate document and an extra blank line at EOF in `AdminPhotographerController.java`.

**Actual:** the tested release can be lost, cannot be recreated from HEAD, and cannot be correlated to the reported build by commit hash.

**Expected:** an intentional reviewed commit/tag contains source, migrations, tests and documentation; CI builds that exact commit; the working tree is clean except for this independent audit report if intentionally kept separate.

**Recommended release action:** after resolving blockers, have the development owner review and checkpoint the intended files, excluding generated/unwanted artifacts, then rerun acceptance on the exact commit. This audit did not commit or alter Git history.

**Verification:** **FAILED**.

## Independent automated test results

### Full suite actually executed

| Item | Result |
|---|---:|
| Maven command | `mvn clean verify` |
| Build | SUCCESS |
| Tests | 902 |
| Passed | 902 |
| Failed | 0 |
| Errors | 0 |
| Skipped | 0 |
| Surefire report files | 121 |
| Execution window | 2026-10-26 02:08:54–02:10:18 +07:00 |
| Runtime | JDK 26.0.2.1 compiling with `release 21`; Maven 3.9.11 |

### The 29 formerly skipped SQL tests

All ran because the audit process supplied an isolated `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`:

| Suite | Tests |
|---|---:|
| `DatabaseConnectionIntegrationTests` | 1 |
| `PhotoConnectApplicationTests` | 1 |
| `BookingRepositoryIntegrationTests` | 2 |
| `DepositRepositoryIntegrationTests` | 2 |
| `PhotographerProfileRepositoryIntegrationTests` | 7 |
| `PortfolioImageRepositoryIntegrationTest` | 8 |
| `SavedPhotographerRepositoryIntegrationTest` | 4 |
| `UserRepositoryIntegrationTests` | 1 |
| `UserServiceTest` | 3 |
| **Total** | **29** |

The fresh database contained 23 application tables and 19 successful Flyway history rows: V001, V008, V010–V014 and V016–V027. Hibernate `ddl-auto=validate` completed.

### What 902 tests do not establish

- `BookingDoubleBookingConcurrencyTest` uses mocked repositories and an in-process lock; it is not a two-transaction SQL Server race test.
- `BookingDisputeConcurrencyContractTest` only checks that `Booking.version` has `@Version`; it does not execute concurrent database updates.
- WebSocket tests invoke `StompSecurityChannelInterceptor` with mocks; no authenticated STOMP clients connected to the running broker during this audit.
- private Cloudinary URL generation is mocked and missed RA-02.
- migration contract tests inspect SQL text and cannot detect the operational danger of startup repair.
- UI “integration contract” tests inspect source/static contracts; they are not browser automation and provide no screenshot evidence.
- no live Supabase/Google, SMTP, Cloudinary or production network integration was exercised.

Real SQL Server booking concurrency/rollback and authenticated WebSocket-client verification therefore remain **BLOCKED** pending dedicated integration tests, even though their unit/contract tests pass.

## Actual HTTP workflow verification

The server was started on localhost:18080 against the isolated SQL Server database. A non-default audit-only seed password was supplied.

### Customer → Photographer → Customer journey

| Step | Evidence | Result |
|---|---|---|
| Customer login | HTTP 302 to `/` with JWT/session | PASS |
| Select package and book | Booking 6, package `Audit Journey Package`, snapshot price VND 2,500,000 | PASS |
| Photographer accept | history `PENDING -> ACCEPTED` by user 38 | PASS |
| Simulated deposit | HTTP flow; deposit `PAID`, VND 750,000; booking `ACCEPTED -> CONFIRMED` | PASS |
| Chat | customer POST 201; photographer POST 201; two persisted messages | PASS |
| Shoot/post-process | `CONFIRMED -> SHOOTING -> POST_PROCESSING` | PASS |
| Private upload | authenticated Cloudinary call attempted; failed closed because credentials were absent; zero photo rows inserted | BLOCKED (external credentials) |
| First delivery | exercised after inserting isolated synthetic authenticated metadata; `POST_PROCESSING -> DELIVERED` | PARTIAL |
| Customer revision | `DELIVERED -> POST_PROCESSING`, reason persisted in booking history | PASS |
| Redelivery | `POST_PROCESSING -> DELIVERED` | PASS |
| Completion | `DELIVERED -> COMPLETED`, album `ACCEPTED` | PASS |
| Review | one 5-star review persisted | PASS |
| Preview/download | concrete Cloudinary URL generation failed; no usable media URL | FAIL (RA-02) |

Synthetic metadata was used only after proving the real upload failed closed. It did not create or represent an external Cloudinary asset. Therefore this is not claimed as a complete production media-delivery test.

### Manager and Admin HTTP boundaries

| Authenticated role | `GET /manager/dashboard` | `GET /admin/dashboard` |
|---|---:|---:|
| Customer | 403 | 403 |
| Photographer | 403 | 403 |
| Manager | 200 | 403 |
| Admin | 403 | 200 |

For the synthetic delivery photo, Manager received 403 from the private preview route. Customer and Admin passed ownership/role authorization but URL generation failed and redirected to the gallery. The exact-role boundary is **VERIFIED** for these actual HTTP probes.

## Configuration and credential review

- Production properties contain empty environment-backed database, Cloudinary, Supabase, mail and JWT secret values; no live credential was found.
- The test-only signing key in `pom.xml:177-180` is explicitly scoped to Surefire and is not the production fallback.
- Missing JWT secret correctly prevents startup.
- The old leaked JWT key is accepted (RA-03).
- The known demo password is shipped and documented (RA-04).
- `photoconnect.jwt.cookie-secure` defaults to false at `application.properties:70-71`; every HTTPS deployment must explicitly set it true. This was not treated as a separate blocker because the property documents the requirement, but deployment validation must enforce it.

## Diagram correspondence review

Inventory is complete: 52 named editable diagrams, 52 SVG exports and 52 PNG exports; the 53rd `.puml` file is the shared theme. Manager routes, support/audit entities, booking lifecycle, simulated deposit and authenticated-delivery intent are broadly represented.

Remaining documentation mismatches:

1. `docs/PHOTOCONNECT_V5_DIAGRAM_INDEX.md:56` says the full ERD contains all 19 persistent entities. Source currently has 22 `@Entity` classes, and `ERD-01-full-database.puml` omits the three V027 entities that appear only in ERD-05.
2. `STATE-02-photographer-approval.puml:9` shows `SUSPENDED -> APPROVED` reinstate, while `AdminPhotographerServiceImpl:75-78` permits approval only from `PENDING`. The index itself contradicts the diagram at line 99.
3. `SEQ-09-private-delivery.puml:9,22-23` names `CloudinaryServiceImpl.generateAuthenticatedUrl`; implementation is `CloudinaryStorageServiceImpl.generateAuthorizedUrl`.
4. `docs/PHOTOCONNECT_V5_DIAGRAM_INDEX.md:130` still reports the old fresh-install V013 blocker. Fresh install now passes, but by editing V013 and masking drift with repair, which is the new release blocker.
5. The delivery diagrams claim a 300-second signed URL, but RA-02 shows that path is not operationally verified.

**Diagram verdict:** **PARTIALLY CORRESPONDING**. Major navigation and state concepts match, but “all verified” is not accurate until the above diagrams and security behavior are reconciled.

## Release recommendation

**NOT READY**

Minimum evidence required for re-audit:

1. remove automatic startup repair and establish an immutable, reviewed V013/fresh-install/upgrade strategy;
2. fix concrete Cloudinary URL generation and verify it against a disposable authenticated asset, including expiry and anonymous denial;
3. reject the exact historical JWT key and remove the default/published demo password;
4. inventory and migrate legacy public delivery assets with explicit authorization, then verify old URLs;
5. checkpoint the intended release in Git and build/test that exact clean commit;
6. add real SQL Server concurrency/rollback and authenticated STOMP-client tests;
7. correct the diagram mismatches;
8. rerun the full HTTP/browser journey with authorized disposable Cloudinary infrastructure and retain non-sensitive evidence.

Automated unit success is strong but cannot override the failed migration-integrity, known-key, private-media and release-reproducibility checks above.
