package com.photoconnect.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Task030QaIntegrationContractTest {

    private String read(String relativePath) throws IOException {
        return Files.readString(Path.of(System.getProperty("user.dir"), relativePath));
    }

    @Test
    void chatJspHasCorrectScopingNoDuplicateDeclarationsAndButtonClass() throws IOException {
        String chatJsp = read("src/main/webapp/WEB-INF/views/chat.jsp");

        // messagesContainer must define server attributes cleanly via dataset
        assertThat(chatJsp)
                .contains("data-booking-id=\"${booking.id}\"")
                .contains("data-current-user-id=\"${currentUserId}\"")
                .contains("data-context-path=\"${pageContext.request.contextPath}\"");

        // Script must be wrapped in IIFE without raw unquoted JSP EL in JS scope
        assertThat(chatJsp)
                .contains("(function () {")
                .contains("const currentUserId = parseInt(messagesContainer.dataset.currentUserId, 10);")
                .doesNotContain("const currentUserId = ${currentUserId};");

        // Must not contain invalid inline ternary in style attribute that triggers CSS parser errors
        assertThat(chatJsp)
                .doesNotContain("style=\"${msg.senderId == currentUserId");

        // Send button must use design system class btn btn-primary instead of obsolete pc-btn-primary
        assertThat(chatJsp)
                .contains("class=\"btn btn-primary\"")
                .doesNotContain("pc-btn-primary");
    }

    @Test
    void bookingDetailGuardsDepositPaidAtDateParsing() throws IOException {
        String bookingDetailJsp = read("src/main/webapp/WEB-INF/views/booking-detail.jsp");

        // Must guard deposit.paidAt so null legacy deposits do not cause JspException
        assertThat(bookingDetailJsp)
                .contains("<c:when test=\"${not empty deposit.paidAt}\">")
                .contains("<div style=\"font-size: 1.1rem;\">Recorded</div>");
    }

    @Test
    void bookingSuccessProvidesDirectLinkToBookingDetail() throws IOException {
        String bookingSuccessJsp = read("src/main/webapp/WEB-INF/views/booking-success.jsp");

        assertThat(bookingSuccessJsp)
                .contains("href=\"${pageContext.request.contextPath}/bookings/${booking.id}\"")
                .contains("View Booking Details");
    }
}
