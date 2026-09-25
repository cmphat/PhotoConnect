package com.photoconnect.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerDashboardUiContractTest {

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    @Test
    void workspaceUsesSiteMeshAndRendersRequiredRealDataSections() throws IOException {
        String jsp = Files.readString(ROOT.resolve("src/main/webapp/WEB-INF/views/customer-dashboard.jsp"));

        assertThat(jsp)
                .doesNotContain("<!DOCTYPE html>", "<html", "<body")
                .contains("Needs Your Attention")
                .contains("Upcoming &amp; Recent Bookings")
                .contains("Recently Saved Photographers")
                .contains("${dashboard.summary.upcomingCount}")
                .contains("${dashboard.bookings}")
                .contains("${dashboard.savedPhotographers}")
                .contains("${booking.actions}")
                .contains("/customer/saved-photographers")
                .contains("/bookings")
                .contains("/photographers")
                .contains("@media (max-width: 900px)")
                .contains("@media (max-width: 640px)")
                .doesNotContain("booking.status ==", "deposit.status ==", "review == null");
    }

    @Test
    void customerNavigationExposesWorkspaceWithoutAdminOrPhotographerControlsInBranch() throws IOException {
        String navbar = Files.readString(ROOT.resolve("src/main/webapp/WEB-INF/views/fragments/navbar.jsp"));
        int customerBranch = navbar.indexOf("<c:otherwise>", navbar.indexOf("sessionScope.userRole == 'PHOTOGRAPHER'"));
        int customerBranchEnd = navbar.indexOf("</c:otherwise>", customerBranch);
        String customerNavigation = navbar.substring(customerBranch, customerBranchEnd);

        assertThat(customerNavigation)
                .contains("/customer/dashboard", ">Dashboard<")
                .contains("/photographers", ">Explore<")
                .contains("/customer/saved-photographers", ">Saved<")
                .contains("/bookings", ">Bookings<")
                .doesNotContain("/admin/", "/photographer/profile", "/photographer/bookings");
    }
}
