package com.photoconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryStorageServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryStorageServiceImpl storageService;

    private static byte[] createValidImageBytes() {
        try {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(200, 200, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final byte[] VALID_PNG_BYTES = createValidImageBytes();

    @BeforeEach
    void setUp() {
        storageService = new CloudinaryStorageServiceImpl(cloudinary);
    }

    @Test
    @DisplayName("uploadPrivateImage: uploads asset strictly as type=authenticated")
    @SuppressWarnings("unchecked")
    void uploadPrivateImage_Success_UsesAuthenticatedType() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> uploadResponse = Map.of(
                "secure_url", "https://res.cloudinary.com/test/image/authenticated/s--xyz--/sample.jpg",
                "public_id", "photoconnect/deliveries/booking_500/sample"
        );
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.png", "image/png", VALID_PNG_BYTES
        );

        CloudinaryStorageService.CloudinaryUploadResult result = storageService.uploadPrivateImage(file, "photoconnect/deliveries/booking_500");

        assertThat(result).isNotNull();
        assertThat(result.publicId()).isEqualTo("photoconnect/deliveries/booking_500/sample");
        assertThat(result.secureUrl()).contains("/image/authenticated/");

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), paramsCaptor.capture());
        Map<String, Object> params = paramsCaptor.getValue();
        assertThat(params.get("type")).isEqualTo("authenticated");
        assertThat(params.get("resource_type")).isEqualTo("image");
    }

    @Test
    @DisplayName("uploadPrivateImage: fail closed without falling back to public upload when authenticated upload fails")
    @SuppressWarnings("unchecked")
    void uploadPrivateImage_FailsClosed_NoPublicFallback() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("Cloudinary private bucket error"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.png", "image/png", VALID_PNG_BYTES
        );

        assertThatThrownBy(() -> storageService.uploadPrivateImage(file, "photoconnect/deliveries/booking_500"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Authenticated Cloudinary upload failed");

        // Verify only 1 upload attempt was made (the authenticated one), and NO public fallback upload was attempted
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    @DisplayName("deleteImage: deletes asset specifying type=authenticated")
    @SuppressWarnings("unchecked")
    void deleteImage_DeletesAuthenticatedAsset() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq("photoconnect/deliveries/booking_500/sample"), any(Map.class)))
                .thenReturn(Map.of("result", "ok"));

        storageService.deleteImage("photoconnect/deliveries/booking_500/sample");

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).destroy(eq("photoconnect/deliveries/booking_500/sample"), paramsCaptor.capture());
        Map<String, Object> params = paramsCaptor.getValue();
        assertThat(params.get("type")).isEqualTo("authenticated");
    }

    @Test
    @DisplayName("migrateAssetToAuthenticated: renames asset from upload to authenticated")
    @SuppressWarnings("unchecked")
    void migrateAssetToAuthenticated_Success() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.rename(eq("legacy_asset"), eq("legacy_asset"), any(Map.class)))
                .thenReturn(Map.of("result", "ok"));

        boolean result = storageService.migrateAssetToAuthenticated("legacy_asset");

        assertThat(result).isTrue();
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).rename(eq("legacy_asset"), eq("legacy_asset"), paramsCaptor.capture());
        Map<String, Object> params = paramsCaptor.getValue();
        assertThat(params.get("type")).isEqualTo("upload");
        assertThat(params.get("to_type")).isEqualTo("authenticated");
    }
}
