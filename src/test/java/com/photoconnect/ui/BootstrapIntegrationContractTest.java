package com.photoconnect.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies TASK-A01 contract compliance for Bootstrap 5 integration:
 * 1. Bootstrap 5.3.3 CSS loads before custom PhotoConnect CSS across all 27 full-page JSPs.
 * 2. Bootstrap 5.3.3 JS bundle loads in shared navigation and synchronizes data-bs-theme.
 * 3. Representative pages genuinely use Bootstrap primitives (grid, forms, alerts, utilities).
 * 4. photoconnect.css maps Bootstrap custom properties to PhotoConnect design tokens.
 */
class BootstrapIntegrationContractTest {

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path VIEWS = ROOT.resolve("src/main/webapp/WEB-INF/views");

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }

    private static final Path DECORATORS = ROOT.resolve("src/main/webapp/WEB-INF/decorators");

    @Test
    void everyFullPageJspLoadsBootstrapBeforeCustomCss() throws IOException {
        // Under TASK-A03 SiteMesh architecture, Bootstrap CSS is centralized in decorators
        // (default.jsp and admin.jsp) and loaded before PhotoConnect custom CSS.
        try (var files = Files.walk(DECORATORS)) {
            List<Path> decorators = files
                    .filter(path -> path.toString().endsWith(".jsp"))
                    .toList();

            assertThat(decorators).isNotEmpty();

            for (Path decorator : decorators) {
                String jsp = Files.readString(decorator);

                assertThat(jsp)
                        .as("Bootstrap 5.3.3 CSS loaded in decorator %s", decorator.getFileName())
                        .contains("bootstrap@5.3.3/dist/css/bootstrap.min.css");

                assertThat(jsp)
                        .as("PhotoConnect custom CSS loaded in decorator %s", decorator.getFileName())
                        .contains("/assets/css/photoconnect.css");

                int bootstrapIndex = jsp.indexOf("bootstrap@5.3.3/dist/css/bootstrap.min.css");
                int customCssIndex = jsp.indexOf("/assets/css/photoconnect.css");

                assertThat(bootstrapIndex)
                        .as("Bootstrap must load before PhotoConnect custom CSS in decorator %s", decorator.getFileName())
                        .isLessThan(customCssIndex);
            }
        }

        // And content JSPs (except self-contained error.jsp) do not duplicate Bootstrap CSS
        try (var files = Files.walk(VIEWS)) {
            List<Path> contentPages = files
                    .filter(path -> path.toString().endsWith(".jsp"))
                    .filter(path -> !path.toString().contains("fragments"))
                    .filter(path -> !path.getFileName().toString().equals("error.jsp"))
                    .toList();

            assertThat(contentPages).hasSize(31);

            for (Path page : contentPages) {
                String jsp = Files.readString(page);
                assertThat(jsp)
                        .as("Content JSP %s must not duplicate Bootstrap CSS import", page.getFileName())
                        .doesNotContain("bootstrap@5.3.3/dist/css/bootstrap.min.css");
            }
        }
    }

    @Test
    void navbarLoadsBootstrapBundleJsAndSynchronizesBsTheme() throws IOException {
        String navbar = read("src/main/webapp/WEB-INF/views/fragments/navbar.jsp");

        assertThat(navbar)
                .as("Bootstrap bundle JS present in navbar")
                .contains("bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js")
                .as("Navbar synchronizes data-bs-theme for light/dark compatibility")
                .contains("data-bs-theme")
                .as("Navbar integrates Bootstrap collapse hook for mobile")
                .contains("data-bs-toggle=\"collapse\"");
    }

    @Test
    void representativePagesGenuinelyUseBootstrapPrimitives() throws IOException {
        String login = read("src/main/webapp/WEB-INF/views/login.jsp");
        String register = read("src/main/webapp/WEB-INF/views/register.jsp");
        String photographers = read("src/main/webapp/WEB-INF/views/photographers.jsp");
        String adminDashboard = read("src/main/webapp/WEB-INF/views/admin-dashboard.jsp");
        String bookingDetail = read("src/main/webapp/WEB-INF/views/booking-detail.jsp");
        String photographerDetail = read("src/main/webapp/WEB-INF/views/photographer-detail.jsp");
        String index = read("src/main/webapp/WEB-INF/views/index.jsp");

        // Login form utilities
        assertThat(login)
                .contains("form-control")
                .contains("form-label")
                .contains("alert-danger")
                .contains("btn-primary");

        // Register form utilities
        assertThat(register)
                .contains("form-control")
                .contains("form-label")
                .contains("form-text")
                .contains("alert-danger")
                .contains("alert-success");

        // Explore (photographers) search grid & form
        assertThat(photographers)
                .contains("row g-3")
                .contains("col-12 col-md-3")
                .contains("form-control")
                .contains("form-label")
                .contains("alert-danger");

        // Admin dashboard KPI grid & alert
        assertThat(adminDashboard)
                .contains("row g-4")
                .contains("col-12 col-md-4")
                .contains("col-12 col-md-6")
                .contains("alert-warning")
                .contains("alert-success")
                .contains("d-flex flex-wrap gap-2");

        // Booking detail layout & alerts
        assertThat(bookingDetail)
                .contains("row g-4")
                .contains("col-12 col-md-6")
                .contains("alert-success")
                .contains("alert-danger");

        // Photographer detail utilities
        assertThat(photographerDetail)
                .contains("d-flex justify-content-between")
                .contains("badge");

        // Home page flex utilities
        assertThat(index)
                .contains("d-flex flex-wrap align-items-center gap-3")
                .contains("d-flex justify-content-between");
    }

    @Test
    void photoconnectCssDefinesBootstrapVariableOverridesAndAlerts() throws IOException {
        String css = read("src/main/resources/static/assets/css/photoconnect.css");

        assertThat(css)
                .as("Bootstrap 5 theme variables in light mode")
                .contains("--bs-primary: var(--accent);")
                .contains("--bs-body-font-family: var(--font-primary);")
                .contains("--bs-body-bg: var(--background);")
                .contains("--bs-body-color: var(--text);")
                .contains("--bs-border-color: var(--border);")
                .contains("--bs-card-bg: var(--surface);")
                .as("Bootstrap 5 theme variables in dark mode")
                .contains("--bs-primary-rgb: 59, 130, 246;")
                .as("Alert classes mapped to PhotoConnect visual identity")
                .contains(".alert {")
                .contains(".alert-success {")
                .contains(".alert-danger {")
                .contains(".alert-warning {");
    }
}
