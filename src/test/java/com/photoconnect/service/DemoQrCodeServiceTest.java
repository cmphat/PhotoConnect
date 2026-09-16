package com.photoconnect.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class DemoQrCodeServiceTest {

    private final DemoQrCodeService service = new DemoQrCodeService();

    @Test
    void generatesLocalSelfContainedQrForHarmlessDemoPayload() {
        DemoQrCodeService.DemoQrCode qr = service.generate(
                42L, new BigDecimal("300000.00"), "PC-20260915-1234ABCD");

        assertThat(qr.payload()).isEqualTo(
                "PHOTOCONNECT-DEMO|BOOKING:42|DEPOSIT:300000.00|REF:PC-20260915-1234ABCD");
        assertThat(qr.payload()).doesNotContain("http", "bank", "account");
        assertThat(qr.dataUri()).startsWith("data:image/svg+xml;base64,");

        String encoded = qr.dataUri().substring("data:image/svg+xml;base64,".length());
        String svg = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        assertThat(svg).contains("<svg", "<path", "fill=\"#111\"");
    }
}
