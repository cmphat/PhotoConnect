package com.photoconnect.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImageValidationUtils — Security & Integrity Test Suite")
class ImageValidationUtilsTest {

    // ── Image Generation Helpers ───────────────────────────────────────────────

    private byte[] createTestRaster(int width, int height, String format) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, format, baos);
        return baos.toByteArray();
    }

    private byte[] createWebpVP8X(int width, int height) {
        byte[] data = new byte[30];
        data[0] = 'R'; data[1] = 'I'; data[2] = 'F'; data[3] = 'F';
        int riffSize = 30 - 8;
        data[4] = (byte) (riffSize & 0xFF);
        data[5] = (byte) ((riffSize >> 8) & 0xFF);
        data[6] = (byte) ((riffSize >> 16) & 0xFF);
        data[7] = (byte) ((riffSize >> 24) & 0xFF);
        data[8] = 'W'; data[9] = 'E'; data[10] = 'B'; data[11] = 'P';
        data[12] = 'V'; data[13] = 'P'; data[14] = '8'; data[15] = 'X';
        data[16] = 10;
        int wMinus1 = width - 1;
        data[24] = (byte) (wMinus1 & 0xFF);
        data[25] = (byte) ((wMinus1 >> 8) & 0xFF);
        data[26] = (byte) ((wMinus1 >> 16) & 0xFF);
        int hMinus1 = height - 1;
        data[27] = (byte) (hMinus1 & 0xFF);
        data[28] = (byte) ((hMinus1 >> 8) & 0xFF);
        data[29] = (byte) ((hMinus1 >> 16) & 0xFF);
        return data;
    }

    private byte[] createWebpVP8L(int width, int height) {
        byte[] data = new byte[26];
        data[0] = 'R'; data[1] = 'I'; data[2] = 'F'; data[3] = 'F';
        int riffSize = 26 - 8;
        data[4] = (byte) (riffSize & 0xFF);
        data[8] = 'W'; data[9] = 'E'; data[10] = 'B'; data[11] = 'P';
        data[12] = 'V'; data[13] = 'P'; data[14] = '8'; data[15] = 'L';
        data[16] = 5;
        data[20] = 0x2F;
        int wMinus1 = (width - 1) & 0x3FFF;
        int hMinus1 = (height - 1) & 0x3FFF;
        int bits = wMinus1 | (hMinus1 << 14);
        data[21] = (byte) (bits & 0xFF);
        data[22] = (byte) ((bits >> 8) & 0xFF);
        data[23] = (byte) ((bits >> 16) & 0xFF);
        data[24] = (byte) ((bits >> 24) & 0xFF);
        return data;
    }

    private byte[] createWebpVP8(int width, int height) {
        byte[] data = new byte[30];
        data[0] = 'R'; data[1] = 'I'; data[2] = 'F'; data[3] = 'F';
        int riffSize = 30 - 8;
        data[4] = (byte) (riffSize & 0xFF);
        data[8] = 'W'; data[9] = 'E'; data[10] = 'B'; data[11] = 'P';
        data[12] = 'V'; data[13] = 'P'; data[14] = '8'; data[15] = ' ';
        data[16] = 10;
        data[20] = 0;
        data[23] = (byte) 0x9D; data[24] = 0x01; data[25] = 0x2A;
        data[26] = (byte) (width & 0xFF);
        data[27] = (byte) ((width >> 8) & 0x3F);
        data[28] = (byte) (height & 0xFF);
        data[29] = (byte) ((height >> 8) & 0x3F);
        return data;
    }

    // ── Happy Path Tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid JPEG within dimensions (200x150) successfully validated")
    void validJpeg_succeeds() throws Exception {
        byte[] bytes = createTestRaster(200, 150, "jpeg");
        MockMultipartFile file = new MockMultipartFile("photo", "portrait.jpg", "image/jpeg", bytes);

        ImageValidationUtils.ImageDimensions dims = ImageValidationUtils.validateImage(file);

        assertThat(dims).isNotNull();
        assertThat(dims.format()).isEqualTo("JPEG");
        assertThat(dims.width()).isEqualTo(200);
        assertThat(dims.height()).isEqualTo(150);
    }

    @Test
    @DisplayName("Valid PNG within dimensions (100x100 boundary) successfully validated")
    void validPng_succeeds() throws Exception {
        byte[] bytes = createTestRaster(100, 100, "png");
        MockMultipartFile file = new MockMultipartFile("photo", "logo.png", "image/png", bytes);

        ImageValidationUtils.ImageDimensions dims = ImageValidationUtils.validateImage(file);

        assertThat(dims).isNotNull();
        assertThat(dims.format()).isEqualTo("PNG");
        assertThat(dims.width()).isEqualTo(100);
        assertThat(dims.height()).isEqualTo(100);
    }

    @Test
    @DisplayName("Valid WebP VP8X within dimensions (400x300) successfully validated")
    void validWebp_VP8X_succeeds() {
        byte[] bytes = createWebpVP8X(400, 300);
        MockMultipartFile file = new MockMultipartFile("photo", "hero.webp", "image/webp", bytes);

        ImageValidationUtils.ImageDimensions dims = ImageValidationUtils.validateImage(file);

        assertThat(dims).isNotNull();
        assertThat(dims.format()).isEqualTo("WEBP");
        assertThat(dims.width()).isEqualTo(400);
        assertThat(dims.height()).isEqualTo(300);
    }

    @Test
    @DisplayName("Valid WebP VP8L within dimensions (250x250) successfully validated")
    void validWebp_VP8L_succeeds() {
        byte[] bytes = createWebpVP8L(250, 250);
        MockMultipartFile file = new MockMultipartFile("photo", "hero.webp", "image/webp", bytes);

        ImageValidationUtils.ImageDimensions dims = ImageValidationUtils.validateImage(file);

        assertThat(dims).isNotNull();
        assertThat(dims.format()).isEqualTo("WEBP");
        assertThat(dims.width()).isEqualTo(250);
        assertThat(dims.height()).isEqualTo(250);
    }

    @Test
    @DisplayName("Valid WebP VP8 within dimensions (180x120) successfully validated")
    void validWebp_VP8_succeeds() {
        byte[] bytes = createWebpVP8(180, 120);
        MockMultipartFile file = new MockMultipartFile("photo", "hero.webp", "image/webp", bytes);

        ImageValidationUtils.ImageDimensions dims = ImageValidationUtils.validateImage(file);

        assertThat(dims).isNotNull();
        assertThat(dims.format()).isEqualTo("WEBP");
        assertThat(dims.width()).isEqualTo(180);
        assertThat(dims.height()).isEqualTo(120);
    }

    // ── Magic Byte & Polyglot Rejection Tests ─────────────────────────────────

    @Test
    @DisplayName("Spoofed MIME type: Executable payload disguised as image/jpeg is rejected")
    void spoofedMimeType_exePayload_rejected() {
        byte[] exeBytes = new byte[]{0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x12, 0x34};
        MockMultipartFile file = new MockMultipartFile("photo", "malicious.jpg", "image/jpeg", exeBytes);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file format");
    }

    @Test
    @DisplayName("Spoofed MIME type: HTML content disguised as image/png is rejected")
    void spoofedMimeType_htmlPayload_rejected() {
        byte[] htmlBytes = "<html><head><title>Phish</title></head><body><h1>Login</h1></body></html>"
                .getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("photo", "fake.png", "image/png", htmlBytes);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file format");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>",
            "<?xml version=\"1.0\"?><svg width=\"200\" height=\"200\"></svg>",
            "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\">",
            "<script>window.location='http://evil.com'</script>"
    })
    @DisplayName("SVG or XML disguised as image/jpeg is strictly rejected")
    void svgAndXmlDisguisedAsImage_rejected(String payload) {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("photo", "image.jpg", "image/jpeg", bytes);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file format");
    }

    @Test
    @DisplayName("Non-image content type (e.g. application/pdf) is rejected")
    void nonImageContentType_rejected() {
        MockMultipartFile file = new MockMultipartFile("doc", "doc.pdf", "application/pdf", new byte[100]);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file format");
    }

    @Test
    @DisplayName("Random binary bytes disguised with image/jpeg MIME type are rejected")
    void randomBytes_rejected() {
        byte[] randomBytes = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55};
        MockMultipartFile file = new MockMultipartFile("photo", "random.jpg", "image/jpeg", randomBytes);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file format");
    }

    // ── Dimension Boundary Tests ──────────────────────────────────────────────

    @Test
    @DisplayName("Image dimensions below 100x100 minimum (50x50) are rejected")
    void imageDimensionsBelowMinimum_rejected() throws Exception {
        byte[] bytes = createTestRaster(50, 50, "png");
        MockMultipartFile file = new MockMultipartFile("photo", "small.png", "image/png", bytes);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image dimensions too small")
                .hasMessageContaining("Minimum resolution is 100x100");
    }

    @Test
    @DisplayName("Image dimensions with one dimension below minimum (120x99) are rejected")
    void imageWidthOkHeightTooSmall_rejected() throws Exception {
        byte[] bytes = createTestRaster(120, 99, "png");
        MockMultipartFile file = new MockMultipartFile("photo", "narrow.png", "image/png", bytes);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image dimensions too small");
    }

    @Test
    @DisplayName("Decompression bomb / excessive dimensions (12000x12000) are rejected safely without OOM")
    void excessiveDimensionsDecompressionBomb_rejected() {
        // Create WebP VP8X header indicating 12000x12000 in only 30 bytes
        byte[] bytes = createWebpVP8X(12000, 12000);
        MockMultipartFile file = new MockMultipartFile("photo", "huge.webp", "image/webp", bytes);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image dimensions exceed maximum allowed limit of 10000x10000");
    }

    // ── File Size & Presence Tests ────────────────────────────────────────────

    @Test
    @DisplayName("Null or empty file is rejected")
    void nullOrEmptyFile_rejected() {
        assertThatThrownBy(() -> ImageValidationUtils.validateImage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Please select an image file to upload");

        MockMultipartFile emptyFile = new MockMultipartFile("photo", "empty.jpg", "image/jpeg", new byte[0]);
        assertThatThrownBy(() -> ImageValidationUtils.validateImage(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Please select an image file to upload");
    }

    @Test
    @DisplayName("File exceeding configured size limit is rejected")
    void oversizedFile_rejected() throws Exception {
        byte[] bytes = createTestRaster(100, 100, "png");
        MockMultipartFile file = new MockMultipartFile("photo", "photo.png", "image/png", bytes);

        // Limit smaller than file size
        long maxSizeBytes = bytes.length - 1;
        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file, maxSizeBytes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds");
    }

    @Test
    @DisplayName("Truncated image missing valid dimensions is rejected")
    void truncatedCorruptedImage_rejected() {
        // PNG header but truncated before IHDR dimensions
        byte[] truncatedPng = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
        MockMultipartFile file = new MockMultipartFile("photo", "truncated.png", "image/png", truncatedPng);

        assertThatThrownBy(() -> ImageValidationUtils.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corrupted or unreadable image file");
    }
}
