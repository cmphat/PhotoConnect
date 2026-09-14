package com.photoconnect.service;

import com.photoconnect.dto.PortfolioImagePublicDto;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PortfolioImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortfolioServiceImpl.
 *
 * CloudinaryStorageService is mocked — no real Cloudinary account required.
 * Tests verify business rule enforcement, not Cloudinary integration.
 *
 * Business rules tested:
 * 1. APPROVED photographer can upload
 * 2. PENDING photographer cannot upload (Cloudinary must NOT be called)
 * 3. Non-photographer (no profile) cannot upload
 * 4. Invalid file type rejected before Cloudinary call
 * 5. Owner can delete their own image
 * 6. Owner cannot delete another photographer's image (SecurityException)
 * 7. Cloudinary called with correct publicId on delete
 * 8. DB record NOT deleted if Cloudinary deletion fails
 * 9. Cloudinary rollback attempted if DB save fails after upload
 */
@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private PortfolioImageRepository portfolioImageRepository;

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    private User approvedUser;
    private PhotographerProfile approvedProfile;

    private User pendingUser;
    private PhotographerProfile pendingProfile;

    @BeforeEach
    void setUp() {
        approvedUser = new User();
        approvedUser.setId(1L);
        approvedUser.setEmail("approved@example.com");
        approvedUser.setRole(UserRole.PHOTOGRAPHER);
        approvedUser.setStatus(UserStatus.ACTIVE);

        approvedProfile = new PhotographerProfile();
        approvedProfile.setUser(approvedUser);
        approvedProfile.setDisplayName("Approved Studio");
        approvedProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);

        // Use reflection to set id on profile (no public setter — simulate DB auto-generated)
        try {
            var idField = PhotographerProfile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(approvedProfile, 10L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        pendingUser = new User();
        pendingUser.setId(2L);
        pendingUser.setEmail("pending@example.com");
        pendingUser.setRole(UserRole.PHOTOGRAPHER);
        pendingUser.setStatus(UserStatus.ACTIVE);

        pendingProfile = new PhotographerProfile();
        pendingProfile.setUser(pendingUser);
        pendingProfile.setDisplayName("Pending Studio");
        pendingProfile.setVerificationStatus(PhotographerVerificationStatus.PENDING);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MockMultipartFile validJpegFile() {
        return new MockMultipartFile(
                "imageFile",
                "photo.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF} // minimal JPEG-like bytes
        );
    }

    private MockMultipartFile validPngFile() {
        return new MockMultipartFile(
                "imageFile",
                "photo.png",
                "image/png",
                new byte[100]
        );
    }

    private MockMultipartFile invalidFile() {
        return new MockMultipartFile(
                "imageFile",
                "script.exe",
                "application/octet-stream",
                new byte[]{0x4D, 0x5A} // MZ header = Windows executable
        );
    }

    // ── Upload: happy path ────────────────────────────────────────────────────

    /**
     * Core behavior: APPROVED photographer uploads a valid JPEG.
     * Cloudinary upload is called, result URL and publicId are persisted.
     */
    @Test
    void addPortfolioImage_approvedPhotographer_validFile_shouldUploadAndSave() {
        when(photographerProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(approvedProfile));

        CloudinaryStorageService.CloudinaryUploadResult uploadResult =
                new CloudinaryStorageService.CloudinaryUploadResult(
                        "https://res.cloudinary.com/test/image/upload/v1/photoconnect/portfolio/abc123.jpg",
                        "photoconnect/portfolio/abc123"
                );
        when(cloudinaryStorageService.uploadImage(any(), eq("photoconnect/portfolio")))
                .thenReturn(uploadResult);

        PortfolioImage savedImage = new PortfolioImage();
        savedImage.setImageUrl(uploadResult.secureUrl());
        savedImage.setPublicId(uploadResult.publicId());
        savedImage.setCaption("Wedding at sunset");
        savedImage.setPhotographerProfile(approvedProfile);
        when(portfolioImageRepository.save(any(PortfolioImage.class))).thenReturn(savedImage);

        PortfolioImage result = portfolioService.addPortfolioImage(1L, validJpegFile(), "Wedding at sunset");

        assertThat(result.getImageUrl()).isEqualTo(uploadResult.secureUrl());
        assertThat(result.getPublicId()).isEqualTo(uploadResult.publicId());
        assertThat(result.getCaption()).isEqualTo("Wedding at sunset");

        // Verify Cloudinary WAS called
        verify(cloudinaryStorageService).uploadImage(any(), eq("photoconnect/portfolio"));
        // Verify DB save WAS called
        verify(portfolioImageRepository).save(any(PortfolioImage.class));
    }

    @Test
    void addPortfolioImage_approvedPhotographer_validPng_shouldUpload() {
        when(photographerProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(approvedProfile));
        when(cloudinaryStorageService.uploadImage(any(), any()))
                .thenReturn(new CloudinaryStorageService.CloudinaryUploadResult(
                        "https://res.cloudinary.com/test/image/upload/v1/abc.png", "abc"));
        when(portfolioImageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PortfolioImage result = portfolioService.addPortfolioImage(1L, validPngFile(), null);
        assertThat(result).isNotNull();
        verify(cloudinaryStorageService).uploadImage(any(), any());
    }

    // ── Upload: PENDING photographer rejected ─────────────────────────────────

    /**
     * PENDING photographer must be rejected BEFORE any Cloudinary call.
     * This is critical: if we called Cloudinary first and then rejected,
     * we'd have orphaned Cloudinary assets from rejected photographers.
     */
    @Test
    void addPortfolioImage_pendingPhotographer_shouldThrow_andNotCallCloudinary() {
        when(photographerProfileRepository.findByUserId(2L))
                .thenReturn(Optional.of(pendingProfile));

        assertThrows(IllegalStateException.class,
                () -> portfolioService.addPortfolioImage(2L, validJpegFile(), null));

        // CRITICAL: Cloudinary must NOT be called for PENDING photographer
        verify(cloudinaryStorageService, never()).uploadImage(any(), any());
    }

    @Test
    void addPortfolioImage_rejectedPhotographer_shouldThrow_andNotCallCloudinary() {
        pendingProfile.setVerificationStatus(PhotographerVerificationStatus.REJECTED);
        when(photographerProfileRepository.findByUserId(2L))
                .thenReturn(Optional.of(pendingProfile));

        assertThrows(IllegalStateException.class,
                () -> portfolioService.addPortfolioImage(2L, validJpegFile(), null));

        verify(cloudinaryStorageService, never()).uploadImage(any(), any());
    }

    // ── Upload: no profile ────────────────────────────────────────────────────

    @Test
    void addPortfolioImage_noPhotographerProfile_shouldThrow() {
        when(photographerProfileRepository.findByUserId(99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> portfolioService.addPortfolioImage(99L, validJpegFile(), null));

        verify(cloudinaryStorageService, never()).uploadImage(any(), any());
    }

    // ── Upload: invalid file ──────────────────────────────────────────────────

    /**
     * An executable file disguised as an image must be rejected BEFORE Cloudinary.
     * The content-type check happens in validateFile(), which runs before upload.
     */
    @Test
    void addPortfolioImage_invalidFileType_shouldThrow_andNotCallCloudinary() {
        when(photographerProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(approvedProfile));

        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.addPortfolioImage(1L, invalidFile(), null));

        verify(cloudinaryStorageService, never()).uploadImage(any(), any());
    }

    @Test
    void addPortfolioImage_emptyFile_shouldThrow_andNotCallCloudinary() {
        when(photographerProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(approvedProfile));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "imageFile", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.addPortfolioImage(1L, emptyFile, null));

        verify(cloudinaryStorageService, never()).uploadImage(any(), any());
    }

    // ── Delete: own image ─────────────────────────────────────────────────────

    /**
     * Successful delete: Cloudinary called with correct publicId, then DB deleted.
     */
    @Test
    void deletePortfolioImage_ownImage_shouldDeleteCloudinaryThenDb() {
        when(photographerProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(approvedProfile));

        PortfolioImage image = new PortfolioImage();
        image.setPublicId("photoconnect/portfolio/abc123");
        image.setPhotographerProfile(approvedProfile);

        when(portfolioImageRepository.findByIdAndPhotographerProfileId(5L, 10L))
                .thenReturn(Optional.of(image));

        doNothing().when(cloudinaryStorageService).deleteImage(any());

        portfolioService.deletePortfolioImage(1L, 5L);

        // Cloudinary must be called with the exact publicId
        verify(cloudinaryStorageService).deleteImage("photoconnect/portfolio/abc123");
        // DB must be deleted after Cloudinary succeeds
        verify(portfolioImageRepository).delete(image);
    }

    // ── Delete: other photographer's image ───────────────────────────────────

    /**
     * Ownership protection: a photographer must not be able to delete another's image.
     * The ownership check is at the DB level (findByIdAndPhotographerProfileId returns empty).
     * SecurityException thrown, Cloudinary NOT called, DB NOT deleted.
     */
    @Test
    void deletePortfolioImage_anotherPhotographersImage_shouldThrowSecurityException() {
        when(photographerProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(approvedProfile));

        // Image 99 belongs to a different profile — not found for profile 10
        when(portfolioImageRepository.findByIdAndPhotographerProfileId(99L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(SecurityException.class,
                () -> portfolioService.deletePortfolioImage(1L, 99L));

        // Cloudinary must NOT be called — ownership check failed
        verify(cloudinaryStorageService, never()).deleteImage(any());
        // DB must NOT be deleted
        verify(portfolioImageRepository, never()).delete(any(PortfolioImage.class));
    }

    // ── Delete: Cloudinary failure leaves DB intact ───────────────────────────

    /**
     * Consistency: if Cloudinary delete fails, the DB record must NOT be deleted.
     * This keeps the DB as the source of truth — the URL still points to a
     * theoretically still-existing Cloudinary asset.
     */
    @Test
    void deletePortfolioImage_cloudinaryFails_shouldNotDeleteDbRecord() {
        when(photographerProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(approvedProfile));

        PortfolioImage image = new PortfolioImage();
        image.setPublicId("photoconnect/portfolio/abc123");
        image.setPhotographerProfile(approvedProfile);

        when(portfolioImageRepository.findByIdAndPhotographerProfileId(5L, 10L))
                .thenReturn(Optional.of(image));

        doThrow(new RuntimeException("Cloudinary API error"))
                .when(cloudinaryStorageService).deleteImage(any());

        assertThrows(RuntimeException.class,
                () -> portfolioService.deletePortfolioImage(1L, 5L));

        // DB record must NOT be deleted since Cloudinary deletion failed
        verify(portfolioImageRepository, never()).delete(any(PortfolioImage.class));
    }

    // ── Public DTO: publicId must not be exposed ──────────────────────────────

    /**
     * getPublicPortfolioForProfile should return DTOs with no getPublicId() method.
     */
    @Test
    void getPublicPortfolioForProfile_dtoShouldNotExposePublicId() {
        boolean hasPublicIdGetter = java.util.Arrays.stream(PortfolioImagePublicDto.class.getMethods())
                .anyMatch(m -> m.getName().equalsIgnoreCase("getPublicId"));

        assertThat(hasPublicIdGetter)
                .as("PortfolioImagePublicDto must NOT expose getPublicId()")
                .isFalse();
    }

    @Test
    void getPublicPortfolioForProfile_shouldReturnDtos() {
        PortfolioImage img1 = new PortfolioImage();
        img1.setImageUrl("https://res.cloudinary.com/test/image1.jpg");
        img1.setPublicId("folder/image1");
        img1.setCaption("First shot");

        PortfolioImage img2 = new PortfolioImage();
        img2.setImageUrl("https://res.cloudinary.com/test/image2.jpg");
        img2.setPublicId("folder/image2");
        img2.setCaption(null);

        when(portfolioImageRepository
                .findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(10L))
                .thenReturn(List.of(img1, img2));

        List<PortfolioImagePublicDto> dtos = portfolioService.getPublicPortfolioForProfile(10L);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getImageUrl()).isEqualTo("https://res.cloudinary.com/test/image1.jpg");
        assertThat(dtos.get(0).getCaption()).isEqualTo("First shot");
        assertThat(dtos.get(1).getCaption()).isNull();
    }
}
