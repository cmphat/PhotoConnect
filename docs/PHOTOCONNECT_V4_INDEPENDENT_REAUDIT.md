# PhotoConnect V4 Independent Security Re-Audit

**Audit date:** 2026-10-25 (Asia/Saigon)  
**Audited checkout:** branch `dev`, HEAD `463e07834b24e792e59095504d8785c60e86b7bb` (`checkpoint: PhotoConnect V4 pre-remediation baseline`)  
**Working tree audited:** 51 modified tracked files and 13 untracked paths on top of HEAD  
**Assessment:** **NOT READY**

## Executive summary

Antigravity's numerical regression claim is accurate: an independent run from an isolated copy executed **812 tests: 783 passed, 0 failed, 0 errors, 29 skipped**, with Maven reporting `BUILD SUCCESS`. The 29 skipped tests were not passed tests; all were disabled because `DB_USERNAME` was absent and all depend on a live SQL Server/application context.

The remediation claim is not substantiated as a whole. Three original findings are verified, five are only partially verified, and two fail re-audit. The most serious unresolved issue is SEC-04: an authenticated Cloudinary upload failure falls back to a public upload, and signed-URL generation failure falls back to a stored direct URL. DATA-01 also remains unresolved because there is no migration runner, production configuration still defaults Hibernate to `update`, and the new "migration" test only scans SQL text.

Two additional defects are release-significant:

1. A known, source-controlled default JWT signing key allows token forgery when `JWT_SECRET` is not supplied.
2. Customer delivery revision requests call a state transition that explicitly rejects customer actors, so the advertised revision journey rolls back/fails in the real service composition.

No application source, configuration, database, dependency, or Git state was changed during this audit. This report is the only repository file created.

## Baseline and test safety

- Single Maven WAR module (`com.photoconnect:photoconnect:0.0.1-SNAPSHOT`), Spring Boot 3.3.0, configured for Java 21 (`pom.xml:7-23`).
- The audit host actually ran Maven 3.9.11 on Oracle JDK 26.0.2.1; compilation used `--release 21`.
- SQL Server is configured at `application.properties:10-17`; Hibernate defaults to `ddl-auto=update`.
- Migrations are loose scripts under `docs/development/migrations`; there is no Flyway/Liquibase dependency or runtime migration configuration (`pom.xml:26-140`).
- The UI is JSP/JSTL with SiteMesh, 46 JSP/XML files, three CSS files, and one static JavaScript file. Midnight Blue tokens remain present in `src/main/resources/static/assets/css/tokens.css:2-23` and `photoconnect.css:11-31`.
- An isolated working-tree copy was created at `C:\Users\ADMIN\AppData\Local\Temp\photoconnect-v4-reaudit-2854dadf4fff4ee7a41b7e4a76aa0358`. `.git`, existing `target` directories, IDE state, and `docs.zip` were excluded. Maven outputs were written only inside that snapshot.
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and all Cloudinary variables were explicitly absent. No service was listening on ports 1433 or 8080, and no container runtime was available. No shared database or application process was contacted.
- `git diff --check` found only five trailing-whitespace warnings in `V008__add_review_status.sql:15-22`; it made no changes.

## Original finding comparison

| ID | Original root cause | Current result | Evidence summary |
|---|---|---|---|
| SEC-01 | Cross-photographer booking mutations | **VERIFIED** | Controller ownership checks and target allow-list plus participant-aware state-machine authorization |
| SEC-02 | Incomplete collision statuses and acceptance races | **PARTIALLY VERIFIED** | Status set and profile lock added; no real concurrent SQL Server test |
| SEC-03 | Unrestricted STOMP subscriptions | **PARTIALLY VERIFIED** | Deny-by-default interceptor added; real broker handshake/subscription not tested and dependencies fail open |
| SEC-04 | Public delivery assets/direct URLs | **FAILED** | Authenticated upload and URL generation both have public/direct fallbacks |
| SEC-05 | Stored XSS in JSP/feed rendering | **PARTIALLY VERIFIED** | Original templates are escaped and static tests pass; no real JSP/browser render was executed |
| LOGIC-01 | Payment marked paid separately from booking confirmation | **PARTIALLY VERIFIED** | One transactional service boundary now exists; rollback/locking not verified on SQL Server |
| LOGIC-02 | Missing/inaccurate booking history | **VERIFIED** | State changes use or explicitly write audit records; cancellation ordering is corrected |
| LOGIC-03 | Dispute resolution accepted non-disputed bookings | **VERIFIED** | Service rejects every status other than `DISPUTED` before writes |
| DATA-01 | Non-repeatable/manual SQL upgrade story | **FAILED** | No executable migration mechanism; `ddl-auto=update`; text-only tests |
| UPLOAD-01 | MIME-header-only image validation | **PARTIALLY VERIFIED** | Magic bytes/dimensions added, but fabricated header-only WebP files are accepted as valid images |

## Detailed findings

### SEC-01 — Cross-photographer booking authorization — VERIFIED (original severity: HIGH)

- **Location:** `src/main/java/com/photoconnect/controller/PhotographerBookingController.java:150-175`; `src/main/java/com/photoconnect/service/BookingStateMachineServiceImpl.java:203-245`; `src/main/java/com/photoconnect/service/BookingServiceImpl.java:344-355`.
- **Root-cause comparison:** The generic photographer endpoint now calls `getBookingForPhotographer` before transition and only permits `SHOOTING` or `POST_PROCESSING`. The state machine now checks booking participation for `DEPOSIT_PENDING` and `EXPIRED` as well as other participant transitions.
- **Reproduction checked:** A photographer session for user 2 targeting user 1's booking returns 403 in controller tests; direct state-machine attempts by another photographer throw `UnauthorizedException`.
- **Actual/expected:** Cross-owner access is denied before mutation, as expected.
- **Tests:** 13 `PhotographerBookingControllerTest` cases and 17 `BookingStateMachineServiceTest` cases passed (including nested suites).
- **Remaining bypass found:** None in the reviewed photographer booking endpoints.
- **Recommended verification:** Retain negative controller and service tests; add one database-backed authorization test when isolated SQL Server is available.

### SEC-02 — Double booking and collision detection — PARTIALLY VERIFIED (original severity: HIGH)

- **Location:** `src/main/java/com/photoconnect/entity/BookingStatus.java:40-69`; `src/main/java/com/photoconnect/service/ScheduleServiceImpl.java:125-170`; `src/main/java/com/photoconnect/service/BookingServiceImpl.java:365-401`; `src/main/java/com/photoconnect/service/RescheduleServiceImpl.java:127-161`; `src/main/java/com/photoconnect/repository/PhotographerProfileRepository.java:20-22`.
- **Root-cause comparison:** Collision queries now include accepted, deposit-pending, confirmed, shooting, post-processing, delivered, and completed statuses. Acceptance and accepted-reschedule paths acquire a pessimistic lock on the photographer profile before rechecking availability.
- **Remaining uncertainty/bypass:** `BookingDoubleBookingConcurrencyTest.java:94-121` creates a Java `ReentrantLock` inside Mockito and manually updates a set. It proves the test simulation, not JPA pessimistic locking, SQL Server isolation, or transaction ordering. It also swallows unexpected exceptions at lines 138-140 and 155-157, reducing diagnostic quality.
- **Reproduction checked:** Overlap/status unit tests pass; the claimed simultaneous acceptance result was not reproduced against a database.
- **Actual/expected:** Source-level serialization is plausible, but exactly one winner under real SQL Server concurrency is unverified.
- **Tests:** `BookingDoubleBookingConcurrencyTest` 2/2 and schedule-related unit tests passed.
- **Recommended fix/verification:** Add a dedicated ephemeral SQL Server test that commits two concurrent acceptance transactions, asserts one `ACCEPTED`, one conflict, and verifies no lock timeout/deadlock. Do not swallow unexpected worker exceptions.

### SEC-03 — WebSocket subscription authorization — PARTIALLY VERIFIED (original severity: CRITICAL)

- **Location:** `src/main/java/com/photoconnect/config/WebSocketConfig.java:21-64`; `src/main/java/com/photoconnect/security/StompSecurityChannelInterceptor.java:58-75,124-211`.
- **Root-cause comparison:** CONNECT now requires a session/JWT identity, user status is checked, notification destinations are user-bound, booking chat subscriptions call participant authorization, and unknown destinations are denied.
- **Remaining bypass/regression risk:** The interceptor is optional in `WebSocketConfig.java:24-34`, so configuration can silently run without subscription security. Booking chat authorization also permits access when `chatService == null` (`StompSecurityChannelInterceptor.java:183-195`). These should fail application startup or deny access, not fail open.
- **Reproduction checked:** Unit frames for anonymous CONNECT, cross-user notification SUBSCRIBE, nonparticipant chat SUBSCRIBE, and arbitrary topics were rejected.
- **Actual/expected:** Interceptor logic behaves as expected in isolation; actual SockJS/raw-WebSocket broker behavior is unverified.
- **Tests:** 13 `StompSecurityChannelInterceptorTest` cases passed.
- **Recommended fix/verification:** Make the interceptor and `ChatService` mandatory, deny when authorization dependencies are unavailable, then run a real STOMP client test against an isolated app.

### SEC-04 — Private Cloudinary delivery — FAILED (severity: CRITICAL)

- **Location:** `src/main/java/com/photoconnect/service/CloudinaryStorageServiceImpl.java:66-85`; `src/main/java/com/photoconnect/service/DeliveryServiceImpl.java:319-355`; `src/main/java/com/photoconnect/dto/DeliveredPhotoDto.java:6-30`.
- **Root cause:** `uploadPrivateImage` catches any Cloudinary authenticated-upload exception and calls public `uploadImage` (`CloudinaryStorageServiceImpl.java:81-84`). Each authorized URL method returns the stored transformed URL when signed generation fails (`DeliveryServiceImpl.java:329,342,355`). The DTO still exposes `secureUrl`, preview, thumbnail, and download URLs.
- **Steps to reproduce:** Configure Cloudinary so authenticated upload fails while ordinary upload succeeds, then upload a delivered photo. Alternatively make signed URL generation return null for an existing public delivery record and request preview/download as an authorized participant.
- **Actual behavior:** A sensitive deliverable is persisted as public storage or the controller redirects to a direct stored URL. Possession of that URL bypasses PhotoConnect authorization and expiry.
- **Expected behavior:** Private upload/signing failures must fail closed; no public delivery URL should be persisted or returned.
- **Test-quality issue:** `PrivateMediaAuthorizationTest.java:114-193` mocks already-signed URLs. It never exercises Cloudinary upload mode, signature validity/expiry, anonymous Cloudinary access, or the fallback branches.
- **Recommended fix:** Remove public/direct fallbacks for delivery assets, persist explicit delivery type, avoid returning storage URLs in DTOs, use authenticated deletion parameters, migrate existing public delivery objects, and verify anonymous access is denied by real Cloudinary.
- **Verification status:** Source-level bypass confirmed; real external Cloudinary verification was BLOCKED by absent isolated credentials.

### SEC-05 — Stored XSS in JSP templates — PARTIALLY VERIFIED (original severity: HIGH)

- **Location:** `src/main/webapp/WEB-INF/views/feed.jsp:432-599`; `src/main/webapp/WEB-INF/views/photographer-packages.jsp` (package text/data attributes); `src/main/webapp/WEB-INF/views/user-profile.jsp:526-564,653-664`; `src/test/java/com/photoconnect/security/StoredXssDefenseTest.java:37-218`.
- **Root-cause comparison:** The original feed/package user strings now use `<c:out>`, inline handlers containing dynamic values were replaced with data attributes/listeners, and dynamically added comment text uses `textContent`.
- **Remaining risk:** The regression test primarily asserts literal template strings. Its "inert payload" cases test Spring's `HtmlUtils` independently and never render a JSP with stored malicious data. Existing database URL values also receive HTML escaping but are not revalidated on read; old `javascript:` values could remain hazardous in link `href` attributes even though new profile setters normalize protocols.
- **Reproduction checked:** Static assertions for the original vulnerable expressions passed; a real JSP/browser payload test was unavailable.
- **Actual/expected:** Source templates appear correctly escaped for the original payloads; real rendered DOM behavior remains unverified.
- **Tests:** 8 nested `StoredXssDefenseTest` cases passed.
- **Recommended verification:** Render JSPs through an isolated servlet container with stored payload fixtures and inspect the parsed DOM. Sanitize/normalize legacy URL fields or allow-list schemes at output.

### LOGIC-01 — Atomic payment settlement — PARTIALLY VERIFIED (original severity: HIGH)

- **Location:** `src/main/java/com/photoconnect/service/DepositServiceImpl.java:106-172`; `src/main/java/com/photoconnect/repository/DepositRepository.java:27-29`.
- **Root-cause comparison:** Deposit settlement, flush, and booking transition now occur inside one `@Transactional` service method. A pessimistic deposit lock serializes duplicate payment posts, and transition exceptions are propagated rather than caught.
- **Remaining uncertainty:** The rollback test at `DepositServiceTest.java:290-300` uses mocks and only observes an exception; it cannot prove the database rolls `PAID` back. Payment/cancellation races also were not exercised on SQL Server.
- **Reproduction checked:** Unit paths for success, duplicate retry, invalid booking state, and propagated transition failure passed.
- **Actual/expected:** Transaction design is correct at source level; persistence atomicity is not independently demonstrated.
- **Recommended verification:** On isolated SQL Server, force history/booking update failure after deposit flush and assert both rows remain pre-payment. Add concurrent duplicate payment/cancellation tests.

### LOGIC-02 — Booking history completeness — VERIFIED (original severity: MEDIUM)

- **Location:** `src/main/java/com/photoconnect/service/BookingStateMachineServiceImpl.java:120-169`; `src/main/java/com/photoconnect/service/BookingServiceImpl.java:227-303,365-451`; `src/main/java/com/photoconnect/service/AdminBookingServiceImpl.java:115-181`; `src/main/java/com/photoconnect/service/RescheduleServiceImpl.java:156-179`.
- **Root-cause comparison:** Cancellation metadata is saved without changing status, then the state machine records the real old-to-cancelled transition. Accept/reject/complete/deposit/delivery routes invoke the state machine. Admin dispute and reschedule changes explicitly record history.
- **Reproduction checked:** Captured history assertions verify old/new status, actor, reason, timestamp, and one-record behavior.
- **Actual/expected:** Reviewed mutation paths now create accurate audit entries. Redundant post-transition `setStatus/save` calls remain but do not create a second history record.
- **Tests:** Booking/state-machine/admin/reschedule unit suites passed.
- **Recommended hardening:** Make `BookingStateMachineService` mandatory and remove fallback/direct duplicate status writes to prevent future bypasses.

### LOGIC-03 — Administrative dispute status validation — VERIFIED (original severity: HIGH)

- **Location:** `src/main/java/com/photoconnect/service/AdminBookingServiceImpl.java:88-113`; `src/main/java/com/photoconnect/controller/AdminBookingController.java:112-149`; `src/main/webapp/WEB-INF/views/admin-booking-detail.jsp:118-152`.
- **Root-cause comparison:** The service validates the current user as admin and requires booking status `DISPUTED` before any booking/deposit/history write. The UI only renders the action form for disputed bookings.
- **Reproduction checked:** PENDING, ACCEPTED, CONFIRMED, COMPLETED, and CANCELLED service cases reject mutation; JSON controller cases return 409.
- **Actual/expected:** Invalid status is rejected with no save calls, as expected.
- **Tests:** 12 service and 13 controller cases passed.
- **Recommended hardening:** Route the final state change through one mandatory state-machine/audit abstraction rather than duplicating it.

### DATA-01 — SQL Server migrations — FAILED (severity: HIGH)

- **Location:** `pom.xml:26-140`; `src/main/resources/application.properties:10-17`; `src/test/java/com/photoconnect/database/DatabaseMigrationRepeatabilityTest.java:20-204`; `docs/development/migrations/V001__baseline_schema.sql` and V008-V021.
- **Root cause:** There is no Flyway/Liquibase/runtime script executor or schema history table. The application still defaults to `spring.jpa.hibernate.ddl-auto=update`, so the documented scripts are not the authoritative deployment path. Versions 002-007, 009, and 015 are absent with no baseline/versioning mechanism.
- **Steps to reproduce:** Start the application against a blank or older SQL Server without manually running docs scripts.
- **Actual behavior:** Hibernate mutates schema opportunistically; nothing records which scripts ran or applies them in order. The "repeatability" test only searches for keywords/table names. Lines 159-193 label string scanning as simulated execution; no SQL parser/server executes the scripts. Its configuration assertion at lines 195-203 explicitly accepts the unsafe `update` default despite claiming to validate `validate`/`none` support.
- **Expected behavior:** A versioned runner applies scripts once, records history/checksums, supports baseline/upgrade, and production uses `ddl-auto=validate` or `none`.
- **Recommended fix:** Install/configure a SQL Server-supported migration runner; relocate scripts to its classpath convention; define baseline policy; change safe defaults; test clean install, upgrade from the last release, second startup, and JPA validation on ephemeral SQL Server.
- **Verification status:** Static failure confirmed; SQL installation/upgrade was BLOCKED because no isolated SQL Server/container exists.

### UPLOAD-01 — Binary image validation — PARTIALLY VERIFIED (original severity: HIGH)

- **Location:** `src/main/java/com/photoconnect/util/ImageValidationUtils.java:68-130,158-345`; call sites in `CloudinaryStorageServiceImpl.java:48-68`, `PortfolioServiceImpl.java:276-277`, `UserProfileServiceImpl.java:219`, and `DeliveryServiceImpl.java:470-471`.
- **Root-cause comparison:** MIME allow-listing is now supplemented by magic-byte checks, dimension bounds, and early text/executable signatures. All upload services invoke the utility.
- **Remaining bypass:** The validator does not fully decode or structurally validate PNG/WebP. The test itself constructs 26-30 byte headers with claimed dimensions and calls them valid (`ImageValidationUtilsTest.java:30-85,118-158`). A fabricated VP8X header therefore passes as an image. Text scanning covers only the first 1024 bytes (`ImageValidationUtils.java:133-145`), so appended/polyglot content is not rejected by this layer.
- **Reproduction checked:** The synthetic header-only WebP "happy paths" passed during this audit.
- **Actual/expected:** Spoofed leading headers can satisfy local validation; expected behavior is successful full decode (and preferably server-side re-encoding) before acceptance.
- **Tests:** 19 validator cases passed, but several encode the bypass as expected success.
- **Recommended fix:** Use a maintained decoder to parse the complete image, enforce pixel/memory/metadata limits, reject trailing/invalid structures where feasible, and re-encode before storage. Keep Cloudinary's resource-type checks as defense in depth, not the primary validator.

## Additional vulnerabilities and remediation regressions

### NEW-01 — Known default JWT signing secret — CRITICAL

- **Location:** `src/main/resources/application.properties:39-46`; `src/main/java/com/photoconnect/config/JwtSecurityConfig.java:26-30`; `src/main/java/com/photoconnect/security/JwtTokenService.java:50-84`.
- **Root cause:** `photoconnect.jwt.secret` contains a public Base64 default. This contradicts the adjacent claim that startup fails if `JWT_SECRET` is absent. `JwtTokenService` only sees a nonblank, sufficiently long key and accepts it.
- **Steps to reproduce:** Run without `JWT_SECRET`; sign an HS token with the committed key containing a known active admin user ID, role `ADMIN`, valid `iat`, `exp`, and `jti`; send it in the authentication cookie.
- **Actual behavior:** Signature verification succeeds and the HTTP filter accepts it when ID/status/role match the database, enabling role impersonation.
- **Expected behavior:** Production must refuse startup without a unique external secret; no usable signing key may be committed.
- **Recommended fix:** Remove the default entirely, require secret injection, rotate any deployment that used it, and add configuration/startup tests proving absence/weakness fails.
- **Verification status:** Source-level exploit precondition confirmed; no live admin token was generated or used.

### NEW-02 — Customer revision workflow is rejected by its own state machine — MEDIUM

- **Location:** `src/main/java/com/photoconnect/service/DeliveryServiceImpl.java:268-299`; `src/main/java/com/photoconnect/service/BookingStateMachineServiceImpl.java:214-219`; `src/test/java/com/photoconnect/service/DeliveryServiceTest.java:330-346`.
- **Root cause:** `requestRevision` invokes `DELIVERED -> POST_PROCESSING` using the customer ID, but state-machine authorization permits `POST_PROCESSING` only for the assigned photographer.
- **Steps to reproduce:** As the booking customer, submit revision notes for a delivered album.
- **Actual behavior:** `UnauthorizedException` is thrown; the surrounding transaction rolls back and the controller reports "Request failed".
- **Expected behavior:** The legitimate customer revision action should atomically set the album to `REVISION_REQUESTED`, return the booking to post-processing, record history, and notify the photographer.
- **Test gap:** The delivery test mocks the state machine and merely verifies that the incompatible call was made; it does not compose the real services.
- **Recommended fix:** Model revision as an explicit authorized transition/action and add a real service-composition regression test.
- **Verification status:** Confirmed by incompatible production-code predicates; browser execution was unavailable.

## Regression suite and skipped-test analysis

**Command executed:** `mvn test -Dstyle.color=never` in the isolated snapshot.  
**Result:** `BUILD SUCCESS` in 42.145 seconds.

| Metric | Antigravity claim | Independent result |
|---|---:|---:|
| Tests executed | 812 | **812** |
| Passed | 783 | **783** |
| Failed | 0 | **0** |
| Errors | 0 | **0** |
| Skipped | 29 | **29** |

All skips report: `Environment variable [DB_USERNAME] does not exist`.

| Skipped suite | Count | Infrastructure/behavior covered |
|---|---:|---|
| `DatabaseConnectionIntegrationTests` | 1 | SQL Server connectivity |
| `PhotoConnectApplicationTests` | 1 | Full Spring context with datasource/schema |
| `BookingRepositoryIntegrationTests` | 2 | Booking persistence/query ordering |
| `DepositRepositoryIntegrationTests` | 2 | Deposit persistence/unique booking constraint |
| `PhotographerProfileRepositoryIntegrationTests` | 7 | Profile persistence, constraints, search |
| `PortfolioImageRepositoryIntegrationTest` | 8 | Portfolio persistence/query behavior |
| `SavedPhotographerRepositoryIntegrationTest` | 4 | Saved-profile persistence/uniqueness |
| `UserRepositoryIntegrationTests` | 1 | User persistence |
| `UserServiceTest` | 3 | Registration paths using Spring/database context |

These tests are not isolated: their setup writes fixed or generated records and teardown deletes records through repositories. They must not be enabled merely by exporting development credentials. They need a dedicated disposable database/profile.

## Integration verification matrix

| Requested verification | Result | Evidence / exact environment needed |
|---|---|---|
| SQL Server clean schema installation | **BLOCKED** | No isolated SQL Server/container; no runtime migration runner exists. Need disposable SQL Server 2019+ and automated script runner. |
| SQL Server upgrade/repeatability | **BLOCKED / static failure** | Text-only test passed; need a fixture at the prior release schema, version tracking, two upgrade runs, and data-preservation assertions. |
| JPA schema validation | **BLOCKED** | Need migrated disposable DB and `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`. Current default is `update`. |
| Concurrent booking acceptance | **BLOCKED** | Mockito simulation passed; need two real transactions on isolated SQL Server. |
| Booking/payment transactional consistency | **BLOCKED** | Unit tests passed; need forced post-flush failure and row assertions in disposable SQL Server. |
| Unauthorized WebSocket subscription rejection | **BLOCKED** | Interceptor unit tests passed; need running isolated app plus real SockJS/raw STOMP clients and authenticated users. |
| Private Cloudinary authorization | **BLOCKED / source failure** | No isolated Cloudinary account; source has public/direct fallback. Need test cloud, authenticated asset, anonymous fetch checks, and expiry checks. |
| Real JSP HTML escaping | **BLOCKED** | Static template tests passed; need running isolated app/database with stored payload fixtures and browser DOM inspection. |

## End-to-end and UI review

No browser journey was claimed as executed. There was no running app, SQL Server, test profile/database, or safe way to seed/mutate shared data. Starting the application would have targeted the configured local SQL Server default and `ddl-auto=update`; this was intentionally not attempted.

| Journey | Source/static review | Browser result |
|---|---|---|
| Customer: Search -> Package -> Booking -> Deposit -> Delivery -> Review | Package price/name/duration/photo-count/delivery-day snapshots are copied at booking creation (`BookingServiceImpl.java:137-187`). Deposit and delivery paths exist. Customer revision is broken (NEW-02). | **BLOCKED** |
| Photographer: Packages -> Availability -> Accept -> Shoot -> Deliver | Package/availability/acceptance/delivery paths exist; collision lock is source-only verified; private delivery fails closed requirement. | **BLOCKED** |
| Admin: Users -> Bookings -> Resolve valid dispute | Admin role and disputed-status service checks exist and unit/controller tests pass. | **BLOCKED** |

The Midnight Blue CSS token files and static UI contract tests remained intact. This is source/static evidence only, not visual browser confirmation.

## Release recommendation

**NOT READY**

Release must be blocked at minimum until:

1. SEC-04 is made fail-closed and verified against real private Cloudinary delivery, including existing-asset migration.
2. The committed JWT default secret is removed and affected deployments rotate keys.
3. DATA-01 has an executable, versioned SQL Server migration path with clean-install, upgrade, repeat-run, and JPA-validation evidence.
4. Real SQL Server concurrency and payment rollback tests pass in disposable infrastructure.
5. The customer revision state-machine incompatibility is corrected and regression-tested with real service composition.
6. WebSocket and JSP/browser integration scenarios are exercised in an isolated application environment.

The passing 783 tests provide useful regression confidence, but they do not establish production readiness or verify the external integrations and database semantics that remain blocked or failed above.
