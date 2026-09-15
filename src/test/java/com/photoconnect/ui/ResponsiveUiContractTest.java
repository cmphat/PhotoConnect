package com.photoconnect.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ResponsiveUiContractTest {

    private static String read(String relativePath) throws IOException {
        return Files.readString(Path.of(System.getProperty("user.dir"), relativePath));
    }

    @Test
    void sharedStylesProvideSafeSizingAndMobileBookingLayouts() throws IOException {
        String css = read("src/main/resources/static/assets/css/photoconnect.css");

        assertThat(css).contains("box-sizing: border-box")
                .contains(".pc-booking-layout")
                .contains("grid-template-columns: minmax(0, 1fr)")
                .contains("input[type=\"date\"].form-input")
                .contains(".pc-scroll-region");
    }

    @Test
    void bookingPagesUseResponsiveClassesAndContextPathSafeActions() throws IOException {
        String form = read("src/main/webapp/WEB-INF/views/booking-form.jsp");
        String detail = read("src/main/webapp/WEB-INF/views/photographer-detail.jsp");

        assertThat(form).contains("class=\"pc-booking-layout\"")
                .contains("class=\"pc-booking-datetime-grid\"")
                .doesNotContain("minmax(400px, 1fr)")
                .contains("${pageContext.request.contextPath}/photographers/${photographer.id}/book");
        assertThat(detail).contains("class=\"booking-sidebar-shell\"")
                .contains("max-width: 1480px")
                .contains("${pageContext.request.contextPath}/photographers/${photographer.id}/book");
    }
}
