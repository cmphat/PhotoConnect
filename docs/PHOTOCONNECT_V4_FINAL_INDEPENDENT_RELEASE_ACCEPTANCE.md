# PhotoConnect V4 Final Independent Release Acceptance

**Assessment date:** 2026-10-25 (Asia/Saigon)  
**Release decision:** **NOT READY**

## Executive conclusion

The reported credential-free regression result is reproducible: an isolated copy produced **826 tests, 797 passed, 0 failures, 0 errors, 29 skipped**, with `BUILD SUCCESS`.

The release is nevertheless not acceptable. When the same snapshot was run against a newly created, uniquely named disposable SQL Server 2022 database, Flyway failed on a clean installation at `V013__create_saved_photographers.sql` with SQL Server error 1785 (multiple cascade paths). All 29 previously skipped tests were enabled, but all 29 errored during Spring context creation. The database-backed result was therefore **826 tests, 797 passed, 0 failures, 29 errors, 0 skipped**, with `BUILD FAILURE`.

Two additional security/release issues remain:

1. The Git `HEAD` is still the pre-remediation checkpoint and contains the former runtime JWT fallback. The working-tree blacklist does not include that exact leaked key; direct construction of `JwtTokenService` with the former committed value succeeded.
2. New delivery uploads fail closed as authenticated Cloudinary assets, but existing `upload`-type delivery records remain public until explicitly migrated. No controller, startup job, SQL migration, or batch invokes the supplied one-photo migration method.

No application source, configuration, migration, dependency, shared application data, or Git history was changed. This report is the only repository artifact created by this acceptance pass.

## Audit baseline and isolation

- Branch: `dev`.
- `HEAD`: `463e07834b24e792e59095504d8785c60e86b7bb`, `checkpoint: PhotoConnect V4 pre-remediation baseline`.
- Before this report was created, the working tree contained 74 modified/untracked entries. All remediation remains uncommitted on top of the pre-remediation commit.
- No tracked file deletion is present in `git diff --diff-filter=D`.
- The reviewed application is one Maven WAR module using Spring Boot 3.3.0 and Java release 21 (`pom.xml:7-23`). The host used Maven 3.9.11 and Oracle JDK 26.0.2.1.
- Flyway Core and SQL Server modules resolve to 10.10.0 (`pom.xml:141-149`). Thirteen classpath migrations exist: V001, V008, V010-V014, and V016-V021. The documentation and classpath copies had matching SHA-256 hashes.
- The filesystem test snapshot was `C:\Users\ADMIN\AppData\Local\Temp\photoconnect-v4-final-d0f4003f563c433ea98b483f231929dd`; build output stayed inside that snapshot.
- The disposable database was `PhotoConnect_QA_3698529812f2` on SQL Server 2022 Developer Edition 16.0.1000.6. A unique temporary SQL login was used. Both database and login were removed in a `finally` block; a follow-up server query found zero `PhotoConnect_QA_*` databases and zero `pcqa_*` logins.
- The existing `PhotoConnect` database and the application already listening on port 8080 were not queried, mutated, or browser-tested.

## Comparison with the supplied reports

| Claim | Independent result |
|---|---|
| 826 total / 797 passed / 29 skipped / no failures | **Confirmed only for the credential-free run.** The 29 database tests were not passed. |
| Flyway schema reaches V021 | **Not valid for a fresh install.** Clean SQL Server migration stops at V013. |
| JPA `validate` succeeds | **Not reached on a clean install** because Flyway fails first. A claimed validation on an already populated local schema is not fresh-install evidence. |
| JWT fallback removed | **Working tree only.** `HEAD` still contains it, and the new blacklist omits the exact old value. |
| Cloudinary delivery is private | **New-upload path verified at source/unit level; legacy assets unresolved; external CDN behavior blocked.** |
| WebSocket isolation verified | **Interceptor unit behavior verified; real authenticated STOMP clients not executed.** |
| Live browser role/XSS checks passed | **Claimed by Antigravity, not independently executed.** The isolated app cannot start from a fresh database. |

The prior independent report (`docs/PHOTOCONNECT_V4_INDEPENDENT_REAUDIT.md:6-17`) correctly withheld release. The later release reports claim the critical blockers are remediated (`docs/PHOTOCONNECT_V4_RELEASE_BLOCKERS.md:300-306`, `docs/PHOTOCONNECT_V4_RELEASE_VERIFICATION.md:20-37`), but their own verification document still identifies the real concurrency, rollback, and 29-test database work as blocked (`docs/PHOTOCONNECT_V4_RELEASE_VERIFICATION.md:35-37,270-276`). This acceptance run resolves the 29-test question negatively.

## Release-blocking findings

### RA-01 — HIGH — Fresh SQL Server installation fails at Flyway V013

- **Location:** `src/main/resources/db/migration/V001__baseline_schema.sql:37-56`; `src/main/resources/db/migration/V013__create_saved_photographers.sql:14-27`; duplicate documentation copy at `docs/development/migrations/V013__create_saved_photographers.sql:14-27`.
- **Root cause:** `photographer_profiles.user_id -> users.id` already has `ON DELETE CASCADE`. V013 adds both `saved_photographers.customer_id -> users.id ON DELETE CASCADE` and `saved_photographers.photographer_profile_id -> photographer_profiles.id ON DELETE CASCADE`. SQL Server rejects the resulting multiple cascade route from `users` to `saved_photographers`.
- **Steps reproduced:** Create an empty disposable SQL Server database and unique login; set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to that database; execute `mvn -B test` from the isolated snapshot.
- **Actual behavior:** Flyway stops at V013 with SQL state `S0000`, error 1785: `FK_saved_photographers_profile` may cause cycles or multiple cascade paths. Spring cannot create `flywayInitializer`/`entityManagerFactory`. All 29 database-gated tests error.
- **Expected behavior:** V001-V021 install in order, Flyway records version 021, Hibernate schema validation succeeds, and database-backed tests run.
- **Recommended fix:** Remove one cascade route (normally use `NO ACTION` for one saved-photographer foreign key and delete explicitly in service/repository logic), then test clean install, second startup, and a defined prior-release upgrade fixture on disposable SQL Server.
- **Verification:** **FAILED — independently reproduced on SQL Server 2022.**

### RA-02 — CRITICAL — Release commit still contains the JWT fallback and the remediation blacklist misses it

- **Location:** committed `HEAD:src/main/resources/application.properties:43`; working tree `src/main/resources/application.properties:52-56`; `src/main/java/com/photoconnect/security/JwtTokenService.java:20-23,33-53`; `src/test/java/com/photoconnect/security/JwtTokenServiceTest.java:92-118`.
- **Root cause:** The actual committed fallback decodes to `thisisaverylongsecretkeyforphotoconnectjwtauthentication`. The two new deny-listed values are different strings. The regression test labels a different value as the old key and therefore cannot detect this omission. In addition, none of the remediation is committed: `HEAD` remains the pre-remediation checkpoint.
- **Steps reproduced:** Read the fallback from `git show HEAD:src/main/resources/application.properties`; pass that exact Base64 value to the compiled working-tree `JwtTokenService` with a 30-minute lifetime.
- **Actual behavior:** JShell printed `OLD_COMMITTED_KEY_ACCEPTED`.
- **Expected behavior:** The exact formerly published runtime key is rejected, affected environments rotate away from it, and the release is built from an auditable remediation commit without a runtime secret.
- **Recommended fix:** Add the exact historical key fingerprint/value to the compromised-key rejection set, correct the test fixture, rotate any deployment that may have used it, and create a clean release commit. Prefer a deployment-time secret scanner and key identifier/rotation policy rather than an ever-growing plaintext list.
- **Verification:** **FAILED.** The working-tree empty default does fail closed at source level (`JwtSecurityConfig.java:26-35`), but that does not resolve the committed/reused-key exposure.

### RA-03 — HIGH — Legacy Cloudinary delivery assets remain publicly addressable

- **Location:** `src/main/java/com/photoconnect/service/CloudinaryStorageServiceImpl.java:67-85,89-114,118-133`; `src/main/java/com/photoconnect/entity/DeliveredPhoto.java:21-25,71-96`; `src/main/java/com/photoconnect/service/DeliveryServiceImpl.java:319-389`; `src/main/java/com/photoconnect/dto/DeliveredPhotoDto.java:18-33`.
- **Root cause:** New uploads correctly request `type=authenticated` and no longer fall back to public upload. However, storage type is inferred from the stored URL. Any legacy `/image/upload/` record is treated as `upload`, and generating a signed, tokenized transformed URL does not make the original public Cloudinary URL private. `migratePhotoToAuthenticated` operates on one ID, has no caller, and is not part of Flyway or startup remediation.
- **Steps to reproduce:** Use a pre-remediation delivered-photo row whose `secure_url` contains `/image/upload/`; access the original stored Cloudinary URL without a PhotoConnect session. External execution was unavailable, but the remaining public type and absent migration invocation are directly evidenced in source.
- **Actual behavior:** DTO direct URLs are redacted and PhotoConnect endpoints authorize callers, but already-known or discoverable legacy CDN URLs remain outside that authorization boundary.
- **Expected behavior:** Every delivery object is authenticated/private before release; legacy public URLs cease working; migration completion is measurable and reversible.
- **Recommended fix:** Persist an explicit delivery/storage type, inventory all delivery rows and provider assets, execute a controlled idempotent migration, verify anonymous GET denial, rotate/remove old public assets/URLs, and prevent release while any delivery row remains `upload`.
- **Verification:** **PARTIALLY VERIFIED / FAILED for legacy data.** Real Cloudinary anonymous access and five-minute expiry remain **BLOCKED** because isolated Cloudinary credentials were absent.

### RA-04 — HIGH — Runtime configuration still contains insecure operational fallbacks

- **Location:** `src/main/resources/application.properties:10-12,45-50,58-59`; contradictory documentation at `README.md:70-82`; `src/main/java/com/photoconnect/service/DepositServiceImpl.java:39`.
- **Root cause:** The default profile embeds database username `photoconnect_dev` and password `Password123!`, enables payment simulation, trusts the local SQL Server certificate, and defaults the JWT cookie `Secure` flag to false. There is no production-specific configuration file or mandatory production guard.
- **Reproduction:** Start with environment variables omitted on a host where the default local database account exists.
- **Actual behavior:** JWT itself is blank/fail-fast in the working tree, but other sensitive operational controls silently take development values. README incorrectly states DB username/password must be supplied.
- **Expected behavior:** Release/production startup fails unless database credentials, HTTPS cookie policy, and real-payment policy are explicitly supplied; development conveniences are isolated to a development profile.
- **Recommended fix:** Move all development defaults to a dedicated local profile, make DB credentials mandatory in the default/release profile, require secure cookies for production, and fail startup if payment simulation is enabled outside an explicit demo profile.
- **Verification:** **FAILED at configuration review; full startup was blocked by RA-01.**

## Original finding status

| Finding | Final status | Evidence |
|---|---|---|
| SEC-01 cross-photographer booking authorization | **VERIFIED (source/unit)** | Ownership checks remain in `PhotographerBookingController.java:150-175` and participant checks in `BookingStateMachineServiceImpl.java:203-262`; relevant tests passed. No database-backed authorization test ran because context startup failed. |
| SEC-02 double booking/collisions | **PARTIALLY VERIFIED** | Profile pessimistic lock and expanded active statuses remain. `BookingDoubleBookingConcurrencyTest.java:94-121` still substitutes a Java `ReentrantLock` for JPA/SQL locking and swallows unexpected exceptions at lines 138-140 and 155-157. Real SQL concurrency was not reached. |
| SEC-03 WebSocket subscription privacy | **PARTIALLY VERIFIED** | Config now throws if the interceptor is absent (`WebSocketConfig.java:60-69`) and chat authorization fails closed (`StompSecurityChannelInterceptor.java:182-200`). Unit frames passed. JWT/user repository dependencies remain optional at lines 49-55, and no real authenticated STOMP handshake/subscription was possible. |
| SEC-04 private Cloudinary delivery | **PARTIALLY VERIFIED / FAILED for legacy assets** | New upload and URL-null paths fail closed; DTO URLs are redacted. RA-03 remains. |
| SEC-05 stored JSP XSS | **PARTIALLY VERIFIED** | Static escaping tests passed. No independently rendered malicious fixture or browser DOM test was possible. Legacy URL-scheme data remains a migration concern. |
| LOGIC-01 atomic payment settlement | **PARTIALLY VERIFIED** | One transaction and pessimistic deposit lock are present (`DepositServiceImpl.java:107-172`, `DepositRepository.java:27-29`). Real rollback/concurrent duplicate settlement was not reached because Flyway failed. |
| LOGIC-02 booking history | **VERIFIED (source/unit)** | Reviewed mutation paths and history assertions passed. |
| LOGIC-03 dispute status validation | **VERIFIED (source/unit)** | Non-`DISPUTED` resolution remains rejected before writes; tests passed. |
| DATA-01 SQL Server migrations | **FAILED** | RA-01. The seven `DatabaseMigrationRepeatabilityTest` cases only inspect strings/files (`DatabaseMigrationRepeatabilityTest.java:60-221`) and passed despite a real SQL syntax/schema failure. |
| UPLOAD-01 binary image validation | **PARTIALLY VERIFIED** | Header/type/dimension checks exist, but PNG/WebP are not fully decoded. `ImageValidationUtils.java:204-278` accepts structurally minimal header-only images; tests intentionally label fabricated 25-30 byte WebP headers as valid (`ImageValidationUtilsTest.java:119-155`). |
| NEW-01 JWT fallback | **FAILED** | RA-02. |
| NEW-02 customer revision/redelivery | **VERIFIED (source/unit), E2E BLOCKED** | Customer `DELIVERED -> POST_PROCESSING` authorization is present (`BookingStateMachineServiceImpl.java:220-245`); request/redelivery logic is at `DeliveryServiceImpl.java:270-316,160-203`. Unit tests passed, but no real CUSTOMER/PHOTOGRAPHER browser flow ran. |

## Tests actually executed

### Run A — isolated snapshot, all external credentials absent

| Metric | Result |
|---|---:|
| Tests | 826 |
| Passed | 797 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 29 |
| Maven status | BUILD SUCCESS |

Skipped inventory: `DatabaseConnectionIntegrationTests` 1; `PhotoConnectApplicationTests` 1; `BookingRepositoryIntegrationTests` 2; `DepositRepositoryIntegrationTests` 2; `PhotographerProfileRepositoryIntegrationTests` 7; `PortfolioImageRepositoryIntegrationTest` 8; `SavedPhotographerRepositoryIntegrationTest` 4; `UserRepositoryIntegrationTests` 1; `UserServiceTest` 3.

### Run B — same snapshot, fresh disposable SQL Server database

| Metric | Result |
|---|---:|
| Tests | 826 |
| Passed | 797 |
| Failures | 0 |
| Errors | 29 |
| Skipped | 0 |
| Maven status | BUILD FAILURE |

All 29 errors share the V013 Flyway context-startup failure. They count as executed errors, not passed tests. The run proves the environment-variable gate opened; it does not validate repository behavior because no test method could obtain a Spring context.

## Integration and end-to-end verification

| Objective | Result | Explanation |
|---|---|---|
| Fresh SQL Server install | **FAILED** | V013 SQL error 1785. |
| Migration upgrade | **BLOCKED** | No versioned prior-release schema fixture is supplied. The shared development database was not used. |
| Flyway ordering/checksums | **PARTIAL** | Thirteen scripts are discoverable and doc/classpath hashes match; execution stops at V013. |
| JPA schema validation | **BLOCKED by failure** | Flyway prevents `entityManagerFactory` creation. |
| 29 gated tests | **FAILED** | 29/29 errored during context startup. |
| Real SQL booking concurrency | **BLOCKED by failure** | Existing test is Mockito simulation, not SQL concurrency. |
| Real SQL payment rollback | **BLOCKED by failure** | Unit exception propagation passed; database rollback not reached. |
| Authenticated WebSocket clients | **BLOCKED by failure** | No isolated application could start. The shared port-8080 process was not used. |
| Cloudinary private delivery/expiry | **BLOCKED externally** | No isolated provider credentials. Source/unit fail-closed path passed; legacy exposure remains. |
| Customer/photographer revision and redelivery | **PARTIAL** | Source/unit paths passed; role-based live flow blocked. |
| Browser booking-to-delivery workflow | **BLOCKED** | Isolated application cannot start from a fresh DB; shared application/data were intentionally not touched. |
| Midnight Blue UI/browser regression | **BLOCKED** | Static assets/templates remain and no tracked file was deleted, but no independent browser render was executed. |

## Git and regression review

- No complete tracked file was deleted. The diff is large (56 tracked files, approximately 2,119 insertions and 588 deletions) and remains uncommitted, so it is not a reproducible release candidate.
- The 797 non-database tests provide useful regression coverage, but many security/UI tests are static template checks or Mockito tests rather than provider/browser/database verification.
- `git diff --check` reports five trailing-whitespace warnings in `docs/development/migrations/V008__add_review_status.sql:15-22`; cosmetic only.
- The repository README says DB credentials must be supplied, while runtime properties provide defaults. Release documentation and executable behavior are not aligned.

## Screenshot evidence

No screenshots were captured. Browser testing was not attempted against the shared application, and the isolated application could not start because the clean Flyway migration failed. Reporting screenshots from Antigravity's claimed session as independent evidence would be misleading.

## Required release gates

1. Correct V013 and demonstrate V001-V021 on a new SQL Server database, repeat startup, and JPA `validate`.
2. Re-run all 826 tests with the database gate enabled and obtain zero failures/errors/skips for the 29 integration cases.
3. Add real SQL Server tests for double-book acceptance and payment rollback/races.
4. Reject the exact historical JWT fallback, rotate affected environments, remove runtime secrets from the release commit, and produce a clean committed candidate.
5. Inventory and migrate every legacy public delivery asset; prove anonymous CDN denial and signed-link expiry with isolated staging credentials.
6. Start the corrected isolated application and execute authenticated STOMP subscription tests plus CUSTOMER/PHOTOGRAPHER booking-to-revision/redelivery browser flows.
7. Separate secure release configuration from demo defaults and make unsafe production settings fail startup.

Until these gates pass, the release recommendation remains **NOT READY**.
