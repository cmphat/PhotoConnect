package com.photoconnect.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PhotographerDashboardUiContractTest {

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    private static String read(String path) throws IOException {
        return Files.readString(ROOT.resolve(path));
    }

    @Test
    void studioIsSiteMeshContentWithRequiredOperationalSections() throws IOException {
        String jsp = read("src/main/webapp/WEB-INF/views/photographer-dashboard.jsp");

        assertThat(jsp)
                .doesNotContain("<!DOCTYPE html>", "<html", "<body", "fragments/navbar.jsp")
                .contains("Photographer Studio", "Needs Your Attention", "Booking Requests",
                        "Upcoming Shoots", "Portfolio Preview", "Profile Health",
                        "Portfolio Health", "Availability", "Reputation", "Quick Actions")
                .contains("/photographer/bookings", "/photographer/portfolio",
                        "/photographer/profile/edit", "/photographer/schedule",
                        "items=\"${shoot.actions}\"")
                .doesNotContain("revenue", "conversion", "analytics", "card data");
    }

    @Test
    void actionsAreServerSelectedAndPaymentRemainsObservationOnly() throws IOException {
        String jsp = read("src/main/webapp/WEB-INF/views/photographer-dashboard.jsp");

        assertThat(jsp)
                .contains("items=\"${booking.actions}\"", "action.method == 'POST'",
                        "items=\"${shoot.actions}\"", "${shoot.depositLabel}")
                .doesNotContain("deposit/checkout", "deposit/pay", "payment_reference",
                        "paymentReference", "failureReason");
    }

    @Test
    void responsiveRulesCoverDesktopTabletAndMobileStacking() throws IOException {
        String jsp = read("src/main/webapp/WEB-INF/views/photographer-dashboard.jsp");

        assertThat(jsp)
                .contains("grid-template-columns: minmax(0, 1.65fr) minmax(280px, .75fr)")
                .contains("@media (max-width: 1024px)")
                .contains("@media (max-width: 768px)")
                .contains("@media (max-width: 480px)")
                .contains("grid-template-columns: 1fr")
                .contains("aria-label=\"Profile completeness\"")
                .contains("alt=\"<c:out value='${image.altText}'/>\"");
    }

    @Test
    void photographerNavigationIsStudioFirstAndExcludesCustomerControls() throws IOException {
        String navbar = read("src/main/webapp/WEB-INF/views/fragments/navbar.jsp");
        int photographerBranch = navbar.indexOf("sessionScope.userRole == 'PHOTOGRAPHER'");
        int customerBranch = navbar.indexOf("<c:otherwise>", photographerBranch);
        String photographerNav = navbar.substring(photographerBranch, customerBranch);

        assertThat(photographerNav)
                .contains(">Studio<", ">Bookings<", ">Portfolio<", ">Availability<")
                .doesNotContain(">Dashboard<", ">Saved<");
    }
}
