package com.photoconnect.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * Enterprise image upload validation utility.
 *
 * Enforces defense-in-depth:
 * 1. File presence and size bounds.
 * 2. MIME type verification against allowed image types.
 * 3. Deep magic byte signature verification (JPEG, PNG, WebP).
 * 4. Rejection of SVG, XML, HTML, scripts, and executable payloads.
 * 5. Safe header-only dimension extraction protecting against decompression bombs (100x100 to 10000x10000 pixels).
 * 6. Detection and rejection of corrupted or truncated image binaries.
 */
public final class ImageValidationUtils {

    private static final Logger log = LoggerFactory.getLogger(ImageValidationUtils.class);

    public static final long DEFAULT_MAX_SIZE_BYTES = 15L * 1024 * 1024; // 15 MB
    public static final int MIN_DIMENSION = 100;
    public static final int MAX_DIMENSION = 10000;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    // PNG signature: 89 50 4E 47 0D 0A 1A 0A
    private static final byte[] PNG_SIGNATURE = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    public record ImageDimensions(int width, int height, String format) {}

    private ImageValidationUtils() {
        // Utility class
    }

    /**
     * Validates image using default 15MB size limit.
     */
    public static ImageDimensions validateImage(MultipartFile file) {
        return validateImage(file, DEFAULT_MAX_SIZE_BYTES);
    }

    /**
     * Comprehensive validation of an uploaded MultipartFile.
     *
     * @param file the uploaded file
     * @param maxSizeBytes maximum permitted file size in bytes
     * @return decoded image dimensions and format
     * @throws IllegalArgumentException if validation fails
     */
    public static ImageDimensions validateImage(MultipartFile file, long maxSizeBytes) {
        if (file == null || file.isEmpty() || file.getSize() == 0) {
            throw new IllegalArgumentException("Please select an image file to upload.");
        }

        if (file.getSize() > maxSizeBytes) {
            long maxMb = maxSizeBytes / (1024 * 1024);
            throw new IllegalArgumentException(
                    "File size exceeds the " + (maxMb > 0 ? maxMb + " MB" : maxSizeBytes + " bytes") +
                    " limit. Please choose a smaller image.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.trim().toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file format. Only JPEG, PNG, and WebP images are allowed.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read uploaded file: " + e.getMessage(), e);
        }

        if (bytes == null || bytes.length < 12) {
            throw new IllegalArgumentException(
                    "Invalid file format. Image file is truncated or too small to contain valid headers.");
        }

        // Scan initial bytes for SVG, XML, HTML, or script injection payloads
        scanForDisallowedTextPayloads(bytes);

        // Detect format via magic byte inspection
        String detectedFormat = detectFormatByMagicBytes(bytes);
        if (detectedFormat == null) {
            throw new IllegalArgumentException(
                    "Invalid file format. Only JPEG, PNG, and WebP images are allowed.");
        }

        // Extract dimensions safely without full image decompression
        ImageDimensions dimensions = extractDimensions(bytes, detectedFormat);
        if (dimensions == null) {
            throw new IllegalArgumentException(
                    "Corrupted or unreadable image file. Unable to decode image dimensions.");
        }

        if (dimensions.width() < MIN_DIMENSION || dimensions.height() < MIN_DIMENSION) {
            throw new IllegalArgumentException(String.format(
                    "Image dimensions too small. Minimum resolution is %dx%d pixels. Current resolution: %dx%d.",
                    MIN_DIMENSION, MIN_DIMENSION, dimensions.width(), dimensions.height()));
        }

        if (dimensions.width() > MAX_DIMENSION || dimensions.height() > MAX_DIMENSION) {
            throw new IllegalArgumentException(String.format(
                    "Image dimensions exceed maximum allowed limit of %dx%d pixels. Current resolution: %dx%d.",
                    MAX_DIMENSION, MAX_DIMENSION, dimensions.width(), dimensions.height()));
        }

        log.debug("Validated image: format={}, width={}, height={}, size={}",
                dimensions.format(), dimensions.width(), dimensions.height(), bytes.length);

        return dimensions;
    }

    private static void scanForDisallowedTextPayloads(byte[] bytes) {
        int checkLength = Math.min(bytes.length, 1024);
        String headerAscii = new String(bytes, 0, checkLength, StandardCharsets.ISO_8859_1).toLowerCase();

        if (headerAscii.contains("<svg") ||
            headerAscii.contains("<?xml") ||
            headerAscii.contains("<!doctype") ||
            headerAscii.contains("<html") ||
            headerAscii.contains("<script") ||
            headerAscii.contains("<body") ||
            headerAscii.contains("xmlns=")) {
            throw new IllegalArgumentException(
                    "Invalid file format. SVG, XML, and HTML files are not allowed.");
        }

        // Check for Windows executable (MZ), Linux ELF, ZIP/JAR, or Java class headers
        if ((bytes[0] == 0x4D && bytes[1] == 0x5A) || // MZ
            (bytes[0] == 0x7F && bytes[1] == 0x45 && bytes[2] == 0x4C && bytes[3] == 0x46) || // ELF
            (bytes[0] == 0x50 && bytes[1] == 0x4B && bytes[2] == 0x03 && bytes[3] == 0x04) || // PK zip
            (bytes[0] == (byte) 0xCA && bytes[1] == (byte) 0xFE && bytes[2] == (byte) 0xBA && bytes[3] == (byte) 0xBE)) { // Class
            throw new IllegalArgumentException(
                    "Invalid file format. Executable or archive binaries are strictly forbidden.");
        }
    }

    private static String detectFormatByMagicBytes(byte[] bytes) {
        // JPEG: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF &&
            (bytes[1] & 0xFF) == 0xD8 &&
            (bytes[2] & 0xFF) == 0xFF) {
            return "JPEG";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (bytes.length >= 8) {
            boolean isPng = true;
            for (int i = 0; i < 8; i++) {
                if (bytes[i] != PNG_SIGNATURE[i]) {
                    isPng = false;
                    break;
                }
            }
            if (isPng) {
                return "PNG";
            }
        }

        // WebP: Bytes 0-3 RIFF, Bytes 8-11 WEBP
        if (bytes.length >= 12 &&
            bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
            bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "WEBP";
        }

        return null;
    }

    private static ImageDimensions extractDimensions(byte[] bytes, String format) {
        try {
            return switch (format) {
                case "PNG" -> parsePngDimensions(bytes);
                case "WEBP" -> parseWebpDimensions(bytes);
                case "JPEG" -> parseJpegDimensions(bytes);
                default -> null;
            };
        } catch (Exception e) {
            log.warn("Error parsing image dimensions for format {}: {}", format, e.getMessage());
            return null;
        }
    }

    private static ImageDimensions parsePngDimensions(byte[] bytes) {
        // PNG IHDR chunk must be at offset 12..23
        if (bytes.length < 24) {
            return null;
        }
        if (bytes[12] != 'I' || bytes[13] != 'H' || bytes[14] != 'D' || bytes[15] != 'R') {
            return null;
        }
        int width = ((bytes[16] & 0xFF) << 24) |
                    ((bytes[17] & 0xFF) << 16) |
                    ((bytes[18] & 0xFF) << 8) |
                    (bytes[19] & 0xFF);
        int height = ((bytes[20] & 0xFF) << 24) |
                     ((bytes[21] & 0xFF) << 16) |
                     ((bytes[22] & 0xFF) << 8) |
                     (bytes[23] & 0xFF);
        if (width <= 0 || height <= 0) {
            return null;
        }
        return new ImageDimensions(width, height, "PNG");
    }

    private static ImageDimensions parseWebpDimensions(byte[] bytes) {
        if (bytes.length < 16) {
            return null;
        }

        String chunkFourCc = new String(bytes, 12, 4, StandardCharsets.US_ASCII);

        switch (chunkFourCc) {
            case "VP8X": {
                // Extended WebP: canvas dimensions at offset 24..29 (24-bit little endian, 1-based)
                if (bytes.length < 30) {
                    return null;
                }
                int width = 1 + ((bytes[24] & 0xFF) |
                                ((bytes[25] & 0xFF) << 8) |
                                ((bytes[26] & 0xFF) << 16));
                int height = 1 + ((bytes[27] & 0xFF) |
                                 ((bytes[28] & 0xFF) << 8) |
                                 ((bytes[29] & 0xFF) << 16));
                return new ImageDimensions(width, height, "WEBP");
            }
            case "VP8L": {
                // Lossless WebP: 1 byte signature 0x2F at offset 20, then 14-bit width-1, 14-bit height-1
                if (bytes.length < 25 || bytes[20] != 0x2F) {
                    return null;
                }
                long b = (bytes[21] & 0xFFL) |
                         ((bytes[22] & 0xFFL) << 8) |
                         ((bytes[23] & 0xFFL) << 16) |
                         ((bytes[24] & 0xFFL) << 24);
                int width = (int) (b & 0x3FFF) + 1;
                int height = (int) ((b >> 14) & 0x3FFF) + 1;
                return new ImageDimensions(width, height, "WEBP");
            }
            case "VP8 ": {
                // Lossy WebP: start code 0x9D 0x01 0x2A at offset 23..25, then 14-bit width, 14-bit height
                if (bytes.length < 30) {
                    return null;
                }
                if ((bytes[23] & 0xFF) != 0x9D || (bytes[24] & 0xFF) != 0x01 || (bytes[25] & 0xFF) != 0x2A) {
                    return null;
                }
                int width = ((bytes[26] & 0xFF) | ((bytes[27] & 0xFF) << 8)) & 0x3FFF;
                int height = ((bytes[28] & 0xFF) | ((bytes[29] & 0xFF) << 8)) & 0x3FFF;
                if (width <= 0 || height <= 0) {
                    return null;
                }
                return new ImageDimensions(width, height, "WEBP");
            }
            default:
                return null;
        }
    }

    private static ImageDimensions parseJpegDimensions(byte[] bytes) {
        int i = 2; // skip FF D8
        while (i < bytes.length - 1) {
            if ((bytes[i] & 0xFF) != 0xFF) {
                i++;
                continue;
            }
            while (i < bytes.length && (bytes[i] & 0xFF) == 0xFF) {
                i++;
            }
            if (i >= bytes.length) break;
            int marker = bytes[i] & 0xFF;
            i++;

            // Standalone markers without payload
            if (marker == 0xD8 || marker == 0xD9 || (marker >= 0xD0 && marker <= 0xD7)) {
                continue;
            }

            if (i + 1 >= bytes.length) break;
            int segmentLength = ((bytes[i] & 0xFF) << 8) | (bytes[i + 1] & 0xFF);
            if (segmentLength < 2) break;

            // SOF markers: SOF0 (0xC0) to SOF15 (0xCF), excluding DHT (0xC4), JPG (0xC8), DAC (0xCC)
            if ((marker >= 0xC0 && marker <= 0xCF) && marker != 0xC4 && marker != 0xC8 && marker != 0xCC) {
                if (i + 6 < bytes.length) {
                    int height = ((bytes[i + 3] & 0xFF) << 8) | (bytes[i + 4] & 0xFF);
                    int width = ((bytes[i + 5] & 0xFF) << 8) | (bytes[i + 6] & 0xFF);
                    if (width > 0 && height > 0) {
                        return new ImageDimensions(width, height, "JPEG");
                    }
                }
                break;
            }
            i += segmentLength;
        }

        // Secondary fallback to standard ImageReader header probe
        return fallbackImageReaderDimensions(bytes, "JPEG");
    }

    private static ImageDimensions fallbackImageReaderDimensions(byte[] bytes, String format) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ImageInputStream iis = ImageIO.createImageInputStream(bais)) {
            if (iis == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis, true, true);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    if (width > 0 && height > 0) {
                        return new ImageDimensions(width, height, format);
                    }
                } finally {
                    reader.dispose();
                }
            }
        } catch (Exception ignored) {
            // Fallback failure will return null, triggering invalid/corrupted image error
        }
        return null;
    }
}
