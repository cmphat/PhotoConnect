package com.photoconnect.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class Task031UiPolishContractTest {

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path VIEWS = ROOT.resolve("src/main/webapp/WEB-INF/views");

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }

    @Test
    void sharedDesignSystemDefinesSemanticTokensAndReusablePrimitives() throws IOException {
        String css = read("src/main/resources/static/assets/css/photoconnect.css");

        assertThat(css)
                .contains("--background:", "--surface:", "--text:", "--muted:", "--border:",
                        "--accent:", "--success:", "--danger:")
                .contains(".btn-primary", ".btn-secondary", ".btn-danger", ".btn-ghost")
                .contains(".form-input", ".form-select", ".pc-input", ".field-error")
                .contains(".status-badge", ".admin-table-shell", ".pc-empty-state")
                .contains("@media (max-width: 991px)", "@media (max-width: 640px)", "focus-visible");
    }

    @Test
    void everyFullPageJspLoadsTheSharedStylesheetAndActionButtonsAreClassed() throws IOException {
        Pattern unclassedButton = Pattern.compile("<button(?![^>]*\\bclass=)[^>]*>", Pattern.CASE_INSENSITIVE);

        try (var files = Files.walk(VIEWS)) {
            List<Path> pages = files
                    .filter(path -> path.toString().endsWith(".jsp"))
                    .filter(path -> !path.toString().contains("fragments"))
                    .toList();

            assertThat(pages).hasSize(27);
            for (Path page : pages) {
                String jsp = Files.readString(page);
                assertThat(jsp)
                        .as("shared stylesheet in %s", page.getFileName())
                        .contains("/assets/css/photoconnect.css");
                assertThat(unclassedButton.matcher(jsp).find())
                        .as("unclassed button in %s", page.getFileName())
                        .isFalse();
            }
        }
    }

    @Test
    void representativeRoutesRemainContextPathSafeAndChatParserContractIsPreserved() throws IOException {
        String booking = read("src/main/webapp/WEB-INF/views/booking-detail.jsp");
        String admin = read("src/main/webapp/WEB-INF/views/admin-users.jsp");
        String chat = read("src/main/webapp/WEB-INF/views/chat.jsp");

        assertThat(booking).contains("${pageContext.request.contextPath}/bookings/${booking.id}/cancel");
        assertThat(admin).contains("${pageContext.request.contextPath}/admin/users/${u.id}/status");
        assertThat(chat)
                .contains("data-context-path=\"${pageContext.request.contextPath}\"")
                .contains("const currentUserId = parseInt(messagesContainer.dataset.currentUserId, 10);")
                .doesNotContain("const currentUserId = ${currentUserId};");
    }

    @Test
    void paymentDisclosurePrintStylesAndLegacyTimestampGuardRemainPresent() throws IOException {
        String checkout = read("src/main/webapp/WEB-INF/views/deposit.jsp");
        String receipt = read("src/main/webapp/WEB-INF/views/deposit-receipt.jsp");
        String paymentCss = read("src/main/resources/static/assets/css/payment.css");

        assertThat(checkout).contains("Demo Payment Environment", "No real money will be transferred.", "30%");
        assertThat(receipt)
                .contains("<c:when test=\"${not empty deposit.paidAt}\">")
                .contains("<c:otherwise>Recorded</c:otherwise>");
        assertThat(paymentCss).contains("@media print", ".payment-primary:focus-visible");
    }
}
