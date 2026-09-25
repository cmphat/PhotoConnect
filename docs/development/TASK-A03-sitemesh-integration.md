# TASK-A03: SiteMesh 3 Decorator Integration

## Overview

TASK-A03 introduces a real SiteMesh 3 decorator architecture to the PhotoConnect V2 application (Spring Boot 3.3.0, Jakarta Servlet / Tomcat 10, Java 21+). Prior to this task, approximately 26 full-page JSPs duplicated the entire HTML shell (`<!DOCTYPE html>`, `<html>`, `<head>`, Bootstrap CSS, PhotoConnect CSS, Google Fonts, `<body>`, `<jsp:include page=".../navbar.jsp" />`, Bootstrap JS bundles, and closing `</body></html>`).

TASK-A03 eliminates this duplication by centralizing shell layout, head assets, navigation, and theme initialization into SiteMesh decorators while preserving all page-specific styles, scripts, JSTL logic, and uncommitted TASK-A01 Bootstrap 5 integration work.

---

## 1. Exact SiteMesh Artifact and Version

- **Artifact:** `org.sitemesh:sitemesh`
- **Version:** `3.3.0-RC1`
- **Scope:** Runtime/compile dependency in `pom.xml`

```xml
<!-- TASK-A03: SiteMesh 3 for Jakarta Servlet (Spring Boot 3.3 / Tomcat 10) -->
<dependency>
    <groupId>org.sitemesh</groupId>
    <artifactId>sitemesh</artifactId>
    <version>3.3.0-RC1</version>
</dependency>
```

### Why it is compatible with Spring Boot 3 / Jakarta Servlet
- Older SiteMesh 3 releases (such as `3.0.1` and `3.2.2`) are built strictly against `javax.servlet.*` APIs. When deployed on Spring Boot 3.3 / Tomcat 10, they trigger `ClassNotFoundException: javax.servlet.Filter` or fail filter registration.
- `org.sitemesh:sitemesh:3.3.0-RC1` is the official release built for **Jakarta EE 10** (`jakarta.servlet.*`). Its `org.sitemesh.config.ConfigurableSiteMeshFilter` implements `jakarta.servlet.Filter` directly, making it natively compatible with Spring Boot 3.3's `FilterRegistrationBean`.

---

## 2. Decorator Architecture & Filter Configuration

### Configuration Mechanism
1. **XML Descriptor:** `src/main/webapp/WEB-INF/sitemesh3.xml`
   - Explicitly defines `<decorator-prefix>/WEB-INF/decorators/</decorator-prefix>`.
   - Maps `default.jsp` to `/*` and `admin.jsp` to `/admin/*` (relative to prefix).
   - Exclusions for static assets, WebSockets, APIs, and error routes.
2. **Spring Boot Filter Registration:** `com.photoconnect.config.SiteMeshConfig`
   - Registers `ConfigurableSiteMeshFilter` via `FilterRegistrationBean<ConfigurableSiteMeshFilter>`.
   - Sets `builder.setDecoratorPrefix("/WEB-INF/decorators/")` and registers relative decorator paths (`default.jsp`, `admin.jsp`).
   - Dispatcher types: `DispatcherType.REQUEST` and `DispatcherType.FORWARD` (essential so that Spring MVC view forwarding to `/WEB-INF/views/*.jsp` is intercepted and decorated).
   - Filter order set to `Ordered.LOWEST_PRECEDENCE`.

### Decorator Structure
- `src/main/webapp/WEB-INF/decorators/default.jsp`
  - Owns the public, customer, and photographer layout shell.
  - Generates `<!DOCTYPE html>`, `<html lang="en" data-theme="dark" data-bs-theme="dark">`.
  - Injects Google Fonts, Bootstrap 5.3.3 CSS, and PhotoConnect CSS in strictly governed order.
  - Extracts content page `<head>` (custom styles, page title, extra scripts) via `<sitemesh:write property='head'/>`.
  - Includes role-aware navigation via `<jsp:include page="/WEB-INF/views/fragments/navbar.jsp" />`.
  - Injects content page `<body>` via `<sitemesh:write property='body'/>`.
  - Loads Bootstrap 5 bundle JS (`/assets/vendor/bootstrap/js/bootstrap.bundle.min.js`) and theme toggle scripts once at the bottom.
- `src/main/webapp/WEB-INF/decorators/admin.jsp`
  - Owns the dedicated admin shell.
  - Shares Bootstrap 5, PhotoConnect CSS, and the dark theme system.
  - Includes dedicated admin navigation via `<jsp:include page="/WEB-INF/views/fragments/admin-navbar.jsp" />`.
  - Wraps content in `<div class="admin-layout">` for sidebar/layout styling.
  - Extracts admin page `<head>` and `<body>` cleanly without exposing admin navigation to non-admin views.

---

## 3. Mapping and Exclusion Strategy

### Path Resolution Semantics (Prefix + Relative Name)
SiteMesh 3's `BaseSiteMeshFilterBuilder.setupDefaults()` automatically initializes `decoratorPrefix = "/WEB-INF/decorators/"`. At request time, `PathBasedDecoratorSelector.convertPaths` maps each configured decorator path by evaluating `"%s%s".formatted(prefix, path.trim())`.

Configuring absolute paths like `/WEB-INF/decorators/default.jsp` resulted in double-prefixing: `"/WEB-INF/decorators/WEB-INF/decorators/default.jsp"`.

The configuration was corrected so that:
- `decorator-prefix` is `/WEB-INF/decorators/`
- Mappings reference `default.jsp` and `admin.jsp`
- Final effective RequestDispatcher paths:
  - Public/Customer/Photographer: `/WEB-INF/decorators/default.jsp`
  - Admin: `/WEB-INF/decorators/admin.jsp`

### Mappings
| Pattern | Decorator Name | Effective Dispatcher Target | Description |
|---|---|---|---|
| `/*` | `default.jsp` | `/WEB-INF/decorators/default.jsp` | Default public, customer, and photographer pages |
| `/admin/*` | `admin.jsp` | `/WEB-INF/decorators/admin.jsp` | Admin console pages |

### Exclusions
| Pattern | Reason |
|---|---|
| `/assets/*` | Static resources (CSS, JS, images, fonts) must never be intercepted or altered |
| `/ws/*` | WebSocket / SockJS handshakes and STOMP messaging transport |
| `/api/*` | REST / JSON API endpoints |
| `/error` | Spring Boot error dispatch route (prevent recursive decoration) |
| `/error/*` | Sub-paths of error handlers |

---

## 4. Bootstrap Centralization

Prior to TASK-A03, TASK-A01 loaded Bootstrap 5 CSS and bundle JS in every individual JSP. Under TASK-A03:
1. **Bootstrap CSS** is loaded **once** in the `<head>` of `default.jsp` and `admin.jsp`.
2. **PhotoConnect CSS** (`/assets/css/photoconnect.css`) loads immediately after Bootstrap CSS to guarantee custom styling overrides.
3. **Page-specific styles** from content JSPs load after PhotoConnect CSS via `<sitemesh:write property='head'/>`.
4. **Bootstrap JS Bundle** is loaded **once** at the closing `</body>` of the decorators.
5. All redundant `<link>` and `<script>` imports for Bootstrap were removed from migrated content JSPs.

---

## 5. Navigation Strategy

Navigation is fully owned by the decorators:
- `default.jsp` includes `/WEB-INF/views/fragments/navbar.jsp`, which contains guest, customer, and photographer role-aware navigation, theme toggles, and Bootstrap mobile collapse controls.
- `admin.jsp` includes `/WEB-INF/views/fragments/admin-navbar.jsp`, preserving the dedicated admin management navigation.
- Individual content JSPs no longer contain `<jsp:include page="...navbar.jsp" />` tags, eliminating duplicate navbars.

---

## 6. Number of JSPs Migrated

A total of **26 full-page JSPs** were migrated to SiteMesh content fragments:

### Public / Customer / Photographer Pages (20 JSPs -> `default.jsp`)
1. `index.jsp` — Landing page
2. `login.jsp` — Authentication login
3. `register.jsp` — Authentication registration
4. `photographers.jsp` — Photographer browse/discovery
5. `photographer-detail.jsp` — Photographer public profile
6. `booking-form.jsp` — Customer booking request form
7. `booking-detail.jsp` — Customer booking detail view
8. `booking-success.jsp` — Booking confirmation screen
9. `bookings.jsp` — Customer booking history list
10. `chat.jsp` — Real-time chat interface
11. `deposit.jsp` — Deposit payment checkout
12. `deposit-receipt.jsp` — Payment success receipt
13. `deposit-result.jsp` — Payment outcome view
14. `photographer-portfolio.jsp` — Photographer portfolio management
15. `photographer-schedule.jsp` — Photographer schedule management
16. `photographer-bookings.jsp` — Photographer booking dashboard
17. `photographer-booking-detail.jsp` — Photographer booking review & status
18. `photographer-onboarding.jsp` — Photographer profile creation wizard
19. `photographer-onboarding-status.jsp` — Application approval status
20. `review-form.jsp` — Customer review submission form

### Admin Pages (6 JSPs -> `admin.jsp`)
21. `admin-dashboard.jsp` — Platform metrics & quick actions
22. `admin-users.jsp` — User management table
23. `admin-photographers.jsp` — Photographer review & approval
24. `admin-photographer-detail.jsp` — Detailed photographer audit
25. `admin-bookings.jsp` — Booking oversight table
26. `admin-reviews.jsp` — Review moderation table

---

## 7. Special Page Handling

### `error.jsp`
- **Treatment:** Excluded from SiteMesh decoration (`/error`, `/error/*` in `sitemesh3.xml`).
- **Rationale:** Spring Boot routes unhandled exceptions and HTTP errors to `/error`. Intercepting this route with SiteMesh filter can trigger recursive decoration if error handling itself fails, or produce mangled response streams. `error.jsp` remains self-contained with its own doctype, CSS, and navbar include.

### `chat.jsp`
- **Treatment:** Stripped outer shell; preserved page-specific head `<style>` and scripts.
- **Verification:** Retains SockJS client, STOMP client, custom bubble styles, and a **single** `currentUserId` extraction (`const currentUserId = parseInt(messagesContainer.dataset.currentUserId, 10);`). No duplicate ID declarations.

### `deposit.jsp` / `deposit-receipt.jsp` / `deposit-result.jsp`
- **Treatment:** Stripped outer shell; preserved `payment.css` and `demo-checkout.js` in `<head>` and bottom scripts.

### `login.jsp` / `register.jsp` (Auth)
- **Treatment:** Evaluated auth pages. They fit cleanly into `default.jsp` with standard public navbar and centered card layout, avoiding an unnecessary `auth.jsp` decorator.

---

## 8. Verification & Tests

### Contract Tests
- `com.photoconnect.ui.SiteMeshIntegrationContractTest`:
  1. `sitemeshConfigurationAndDecoratorsExist` — Confirms `sitemesh3.xml`, `SiteMeshConfig.java`, `default.jsp`, and `admin.jsp` exist.
  2. `defaultDecoratorLoadsBootstrapBeforePhotoConnectCss` — Asserts strict CSS load order, title extraction, and navbar inclusion.
  3. `adminDecoratorIsDistinctFromDefault` — Asserts `admin-navbar.jsp` inclusion and isolation from default navbar.
  4. `migratedJspPagesDoNotContainOuterHtmlShell` — Verifies all 26 migrated JSPs have no `<!DOCTYPE html>`, `<html>`, `<head>`, or `<body>` tags.
  5. `errorPageIsExcludedFromSiteMeshDecoration` — Asserts `error.jsp` remains self-contained and excluded.
  6. `criticalPageScriptsAndStylesPreserved` — Asserts chat STOMP/SockJS, deposit checkout JS/CSS, and portfolio upload drawer panel scripts remain intact.
  7. `taskA01BootstrapPrimitivesRetainedAcrossMigratedPages` — Asserts Bootstrap classes (`row`, `col-*`, `form-control`, `alert-*`) from TASK-A01 remain present.
- Updated `BootstrapIntegrationContractTest` and `Task031UiPolishContractTest` to align with decorator-centralized CSS/JS architecture.

### Test Execution Results
- `mvn test`:
  - Total tests run: 419
  - Failures: 0
  - Errors: 0
  - Skipped: 23
  - Result: **BUILD SUCCESS**
- `mvn clean package`:
  - Result: **BUILD SUCCESS**

---

## 9. Manual Browser Verification Checklist

The following routes are prepared for human browser verification when a live database environment is available:
- [ ] `/` — Home landing page (renders with default decorator, navbar, hero, dark theme)
- [ ] `/photographers` — Photographer explore/filter page
- [ ] `/login` — Login screen
- [ ] `/register` — Register screen
- [ ] `/photographers/{id}` — Photographer profile detail view
- [ ] `/bookings/{id}` — Customer booking detail view
- [ ] `/chat/{bookingId}` — Real-time chat interface (STOMP connection, message bubble styling)
- [ ] `/deposit/{bookingId}` — Deposit checkout page (QR code, demo payment script)
- [ ] `/admin/dashboard` — Admin dashboard (admin decorator, admin navbar, stats cards)
- [ ] `/error` or `404` — Error handling page (self-contained layout, no recursive decoration)
