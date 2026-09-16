package com.photoconnect.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/** Generates a self-contained SVG QR without calling a bank or remote image API. */
@Service
public class DemoQrCodeService {

    public DemoQrCode generate(Long bookingId, BigDecimal amount, String transactionReference) {
        String payload = "PHOTOCONNECT-DEMO|BOOKING:" + bookingId
                + "|DEPOSIT:" + amount.toPlainString()
                + "|REF:" + transactionReference;
        try {
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 240, 240,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                            EncodeHintType.MARGIN, 2));
            String svg = toSvg(matrix);
            String dataUri = "data:image/svg+xml;base64,"
                    + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
            return new DemoQrCode(payload, dataUri);
        } catch (WriterException ex) {
            throw new IllegalStateException("Unable to generate the local demo QR.", ex);
        }
    }

    private String toSvg(BitMatrix matrix) {
        StringBuilder path = new StringBuilder();
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    path.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
                }
            }
        }
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 "
                + matrix.getWidth() + " " + matrix.getHeight() + "\" shape-rendering=\"crispEdges\">"
                + "<rect width=\"100%\" height=\"100%\" fill=\"#fff\"/>"
                + "<path d=\"" + path + "\" fill=\"#111\"/></svg>";
    }

    public record DemoQrCode(String payload, String dataUri) {
    }
}
