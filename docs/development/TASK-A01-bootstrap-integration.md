# TASK-A01 — Bootstrap 5 Integration for PhotoConnect

## 1. Requirement & Background

The authoritative PhotoConnect project specification explicitly defines the tech stack:
> *"Xây dựng website kết nối nhiếp ảnh gia và khách hàng PhotoConnect bằng Spring Boot + JSP/JSTL + Bootstrap + JPA + SQLServer/MySQL/PostgreSQL + WebSocket + Decorator SiteMesh + JWT + Cloudinary."*

A project-wide architectural audit conducted prior to this task revealed that Bootstrap was completely **ABSENT** from the codebase (no dependencies, no CSS links, no JS bundle).

**TASK-A01** brings the application into compliance with the official requirement by integrating **Bootstrap 5.3.x** into the existing Spring Boot + JSP/JSTL architecture while preserving the custom PhotoConnect visual identity.

---

## 2. Bootstrap Version & Distribution

- **Bootstrap Version:** `5.3.3` (official latest stable 5.3.x release).
- **CSS Distribution:**
  ```html
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  ```
- **JS Bundle Distribution (includes Popper):**
  ```html
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  ```

---

## 3. Integration Architecture & Loading Order

Because SiteMesh (TASK-A03) does not exist yet, Bootstrap is safely integrated via the current shared JSP and layout mechanisms:

1. **CSS Loading Order (Enforced Across All 27 Full-Page JSPs):**
   - **First:** Bootstrap 5.3.3 CSS (`bootstrap.min.css`)
   - **Second:** PhotoConnect custom CSS (`photoconnect.css`)
   - **Third:** Page-specific `<style>` blocks

   This strict ordering ensures that Bootstrap provides the underlying layout, grid, and utility infrastructure, while PhotoConnect's bespoke styles intentionally override Bootstrap defaults.

2. **JS Bundle Loading:**
   - Loaded in `src/main/webapp/WEB-INF/views/fragments/navbar.jsp`, which is included near the top of `<body>` in all 27 JSPs.
   - Makes `window.bootstrap` (Popper, Collapse, Dropdown, Modal, Toast, Offcanvas) available globally across all routes.

---

## 4. Class Collision Audit & Resolution

| Class / Component | Pre-existing State | Collision Resolution |
| :--- | :--- | :--- |
| `.btn`, `.btn-primary`, `.btn-secondary`, `.btn-danger`, `.btn-sm`, `.btn-lg` | Extensively defined in `photoconnect.css` with dark navy / royal cobalt palette, custom typography, subtle elevations. | **PhotoConnect Wins.** Because `photoconnect.css` loads after Bootstrap, PhotoConnect rules override Bootstrap's default blue buttons and border-radii. In addition, `--bs-primary` is bound to `var(--accent)` (royal cobalt `#2563EB` in light, `#3B82F6` in dark). |
| `.form-control` | Defined in `photoconnect.css` (46px height, elevated surface, custom border & focus glow). | **PhotoConnect Wins.** PhotoConnect input styling wins while Bootstrap form primitives (`.form-label`, `.form-text`, `.mb-3`, `.form-check`) seamlessly augment the forms. |
| `.badge` | Defined in `photoconnect.css` with uppercase tracking and custom status modifiers (`.badge-pending`, `.badge-accepted`, etc.). | **PhotoConnect Wins.** Status badges maintain bespoke styling. Bootstrap badge utilities coexist without conflicts. |
| `.alert`, `.alert-success`, `.alert-danger`, `.alert-warning` | Partially defined (`.alert-banner`, `.alert-success`, `.alert-error` in CSS; `.pc-alert` in JSPs). | **Adapted to Bootstrap.** Explicit rules added in `photoconnect.css` for `.alert`, `.alert-success`, `.alert-danger`, `.alert-warning`, `.alert-info` adopting PhotoConnect's left accent line, background tints, and typography. |
| `.container` | Not defined in `photoconnect.css` (PhotoConnect uses `.editorial-container`). | **No Collision.** Bootstrap `.container` and `.container-fluid` coexist with `.editorial-container`. |
| `.row`, `.col-*` | Not defined in `photoconnect.css`. | **No Collision.** Bootstrap responsive grid is adopted across representative pages. |
| `.card` | Not defined as a standalone class in `photoconnect.css` (PhotoConnect uses `.photographer-card`, `.kpi-card`, `.session-card`). | **No Collision.** Card custom properties (`--bs-card-bg`, `--bs-card-border-color`) are mapped to PhotoConnect design tokens. |
| `.navbar` | Not defined in `photoconnect.css` (uses `.nav-container`, `.nav-left`, `.nav-right`). | **No Collision.** Custom desktop navbar preserved; mobile menu enhanced with Bootstrap collapse. |

---

## 5. Bootstrap Features & Primitives Actually Used

Bootstrap is actively and genuinely used across the application:

1. **Responsive Grid (`row`, `col-*`, `g-*`):**
   - **Search Toolbar (`photographers.jsp`):** Rebuilt with `row g-3 align-items-end`, `col-12 col-md-3`, `col-12 col-md-2`, `col-12 col-md-auto`.
   - **Admin Dashboard KPI Cards (`admin-dashboard.jsp`):** Rebuilt with `row g-4 mb-4`, `col-12 col-md-4`, `col-12 col-md-6`.
   - **Booking Session Specifications (`booking-detail.jsp`):** Rebuilt with `row g-4`, `col-12 col-md-6`, `col-12`.
2. **Form Controls & Layout:**
   - Form field spacing: `.mb-3`, `.mb-4` across `login.jsp`, `register.jsp`, `photographers.jsp`.
   - Form labels: `.form-label` across all forms.
   - Form inputs: `.form-control` applied to text inputs, email, password, tel, and search fields.
   - Form helper text: `.form-text`.
   - Full-width submit buttons: `.w-100` combined with `.btn .btn-primary`.
3. **Alerts:**
   - Standardized feedback banners using `.alert .alert-danger`, `.alert .alert-success`, `.alert .alert-warning` across auth, booking, and administrative views.
4. **Flex & Spacing Utilities:**
   - `.d-flex`, `.flex-wrap`, `.align-items-center`, `.justify-content-between`, `.gap-2`, `.gap-3` used across `index.jsp`, `photographers.jsp`, `admin-dashboard.jsp`, and `photographer-detail.jsp`.
5. **Mobile Navigation & Interactivity:**
   - Bootstrap Collapse data attributes (`data-bs-toggle="collapse"`, `data-bs-target="#navLinksMenu"`) wired into `navbar.jsp`.

---

## 6. Visual Identity & Theme Compatibility

PhotoConnect's bespoke art direction remains intact:
- **Typography:** `Plus Jakarta Sans` for body / UI, `Playfair Display` for editorial headings.
- **Color Palette:** Slate-tinted light canvas (`#F8FAFC`), deep obsidian dark canvas (`#070B12`), royal cobalt / electric blue accent (`#2563EB` / `#3B82F6`).
- **Dark/Light Mode Sync:**
  - Bootstrap 5.3 CSS custom properties (`--bs-primary`, `--bs-body-bg`, `--bs-body-color`, `--bs-border-color`, `--bs-card-bg`, etc.) are mapped to PhotoConnect design tokens in `:root`, `:root[data-theme="light"]`, and `:root[data-theme="dark"]`.
  - The theme switcher in `navbar.jsp` synchronizes both `data-theme` and `data-bs-theme="dark|light"` on `document.documentElement`, ensuring native Bootstrap components render seamlessly in dark mode without white flashes or contrast issues.

---

## 7. Test Results

- **Contract Tests (`BootstrapIntegrationContractTest.java`):**
  - Verifies Bootstrap 5.3.3 CSS loads before `photoconnect.css` across all 27 full-page JSPs.
  - Verifies `bootstrap.bundle.min.js`, `data-bs-theme`, and collapse hooks in `navbar.jsp`.
  - Verifies genuine usage of Bootstrap primitives (`row`, `col-*`, `form-control`, `form-label`, `alert-*`, `mb-3`, `gap-*`) in representative JSPs.
  - Verifies CSS variable overrides and alert rules in `photoconnect.css`.
- **`mvn test`:**
  - 0 failures, 0 errors, BUILD SUCCESS.
- **`mvn clean package`:**
  - BUILD SUCCESS (generates `target/photoconnect.war`).

---

## 8. Manual Verification Status

| Route | Viewport 1440px | Viewport 768px | Viewport 390px | Status |
| :--- | :--- | :--- | :--- | :--- |
| `/` (Home) | Pending human review | Pending human review | Pending human review | **PENDING** |
| `/photographers` (Explore) | Pending human review | Pending human review | Pending human review | **PENDING** |
| `/photographers/{id}` (Photographer Detail) | Pending human review | Pending human review | Pending human review | **PENDING** |
| `/login` (Sign In) | Pending human review | Pending human review | Pending human review | **PENDING** |
| `/register` (Sign Up) | Pending human review | Pending human review | Pending human review | **PENDING** |
| `/bookings/{id}` (Booking Detail) | Pending human review | Pending human review | Pending human review | **PENDING** |
| `/admin/dashboard` (Admin Overview) | Pending human review | Pending human review | Pending human review | **PENDING** |

> [!NOTE]
> Manual verification remains **PENDING** until the user inspects the running application at 1440px, 768px, and 390px viewports.

---

## 9. Next Steps in Roadmap
- **TASK-A02:** JWT Implementation (Not started).
- **TASK-A03:** SiteMesh Decorator Implementation (Not started; will centralize Bootstrap loading inside SiteMesh decorators).
