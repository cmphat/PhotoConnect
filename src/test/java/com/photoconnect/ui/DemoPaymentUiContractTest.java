package com.photoconnect.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DemoPaymentUiContractTest {

    private String read(String relativePath) throws IOException {
        return Files.readString(Path.of(System.getProperty("user.dir"), relativePath));
    }

    @Test
    void checkoutIsExplicitlyDemoAndContainsBothProfessionalMethods() throws IOException {
        String checkout = read("src/main/webapp/WEB-INF/views/deposit.jsp");

        assertThat(checkout)
                .contains("Demo Payment Environment", "No real money will be transferred.",
                        "Demo QR / Bank Transfer", "Demo Card", "Confirm Demo Payment",
                        "4242 4242 4242 4242", "4000 0000 0000 0002")
                .doesNotContain("Simulate Payment", "fake payment", "simulate DB update");
    }

    @Test
    void receiptContainsRequiredAuthoritativeFieldsAndPrintMode() throws IOException {
        String receipt = read("src/main/webapp/WEB-INF/views/deposit-receipt.jsp");
        String css = read("src/main/resources/static/assets/css/payment.css");

        assertThat(receipt).contains("Payment Receipt", "Transaction reference", "Payment method",
                "Customer", "Photographer", "Booking ID", "Session title", "Session date",
                "Agreed price", "Deposit percentage", "Deposit paid", "Remaining balance",
                "Payment timestamp", "Demo transaction — no real money was transferred.",
                "Back to Booking", "My Bookings");
        assertThat(css).contains("@media print");
    }
}
