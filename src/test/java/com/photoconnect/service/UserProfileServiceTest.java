package com.photoconnect.service;

import com.photoconnect.dto.UserProfileDto;
import com.photoconnect.dto.UserProfileUpdateRequest;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.ReviewRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);
        owner.setFullName("Alex Rivera");
        owner.setEmail("alex@example.com");
        owner.setPhone("0987654321");
        owner.setUsername("alexrivera");
        owner.setRole(UserRole.PHOTOGRAPHER);
        owner.setStatus(UserStatus.ACTIVE);
        owner.setBio("Editorial and fashion photographer based in Tokyo.");
        owner.setLocation("Tokyo, Japan");
        owner.setWebsiteUrl("https://alexrivera.studio");
        owner.setInstagramUrl("https://instagram.com/alexrivera");
    }

    @Test
    void getProfile_returnsOwnerDetails_whenViewerIsOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(photographerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        UserProfileDto dto = userProfileService.getProfile(1L, 1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("Alex Rivera");
        assertThat(dto.isOwner()).isTrue();
        assertThat(dto.getEmail()).isEqualTo("alex@example.com");
        assertThat(dto.getPhone()).isEqualTo("0987654321");
        assertThat(dto.getBio()).isEqualTo("Editorial and fashion photographer based in Tokyo.");
        assertThat(dto.getLocation()).isEqualTo("Tokyo, Japan");
        assertThat(dto.getWebsiteUrl()).isEqualTo("https://alexrivera.studio");
        assertThat(dto.getInstagramUrl()).isEqualTo("https://instagram.com/alexrivera");
    }

    @Test
    void getProfile_sanitizesSensitiveFields_whenViewerIsVisitor() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(photographerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        UserProfileDto dto = userProfileService.getProfile(1L, 2L);

        assertThat(dto).isNotNull();
        assertThat(dto.isOwner()).isFalse();
        // Email and phone number must be stripped for non-owner public visitor
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPhone()).isNull();
        // Public bio and socials should remain accessible
        assertThat(dto.getBio()).isEqualTo("Editorial and fashion photographer based in Tokyo.");
        assertThat(dto.getWebsiteUrl()).isEqualTo("https://alexrivera.studio");
    }

    @Test
    void updateProfile_validatesAndUpdatesFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(photographerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setDisplayName("Alexander Rivera");
        req.setBio("Updated bio description.");
        req.setLocation("Kyoto, Japan");
        req.setWebsiteUrl("https://alex.design");
        req.setInstagramUrl("https://instagram.com/alex.new");
        req.setFacebookUrl("https://facebook.com/alexrivera");

        UserProfileDto updated = userProfileService.updateProfile(1L, req);

        assertThat(updated.getDisplayName()).isEqualTo("Alexander Rivera");
        assertThat(updated.getBio()).isEqualTo("Updated bio description.");
        assertThat(updated.getLocation()).isEqualTo("Kyoto, Japan");
        assertThat(updated.getWebsiteUrl()).isEqualTo("https://alex.design");
        assertThat(updated.getInstagramUrl()).isEqualTo("https://instagram.com/alex.new");
        assertThat(updated.getFacebookUrl()).isEqualTo("https://facebook.com/alexrivera");
        verify(userRepository).save(owner);
    }

    @Test
    void updateProfile_rejectsUnsafeUrls() {
        UserProfileUpdateRequest req = new UserProfileUpdateRequest();

        assertThatThrownBy(() -> req.setWebsiteUrl("javascript:alert(1)"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only HTTP and HTTPS are permitted");
    }

    @Test
    void removeAvatar_clearsAvatarUrlAndPublicId() {
        owner.setAvatarUrl("https://res.cloudinary.com/demo/image/upload/v1234/avatar.jpg");
        owner.setAvatarPublicId("avatars/user_1_abc");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.removeAvatar(1L);

        assertThat(owner.getAvatarUrl()).isNull();
        assertThat(owner.getAvatarPublicId()).isNull();
        verify(cloudinaryStorageService).deleteImage("avatars/user_1_abc");
    }

    @Test
    void updateAvatar_validImage_uploadsAndUpdatesUser() throws Exception {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(150, 150, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", baos);
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "avatarFile", "avatar.png", "image/png", baos.toByteArray());

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(cloudinaryStorageService.uploadImage(any(), eq("photoconnect/avatars")))
                .thenReturn(new CloudinaryStorageService.CloudinaryUploadResult("https://res.cloudinary.com/demo/avatar.png", "avatars/alex_123"));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String url = userProfileService.updateAvatar(1L, file);

        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/avatar.png");
        assertThat(owner.getAvatarUrl()).isEqualTo("https://res.cloudinary.com/demo/avatar.png");
        assertThat(owner.getAvatarPublicId()).isEqualTo("avatars/alex_123");
        verify(cloudinaryStorageService).uploadImage(any(), eq("photoconnect/avatars"));
    }

    @Test
    void updateAvatar_spoofedExeDisguisedAsJpeg_rejected() {
        byte[] exeBytes = new byte[]{0x4D, 0x5A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "avatarFile", "avatar.jpg", "image/jpeg", exeBytes);

        assertThatThrownBy(() -> userProfileService.updateAvatar(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file format");
    }

    @Test
    void updateAvatar_dimensionsTooSmall_rejected() throws Exception {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(50, 50, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "jpeg", baos);
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "avatarFile", "avatar.jpg", "image/jpeg", baos.toByteArray());

        assertThatThrownBy(() -> userProfileService.updateAvatar(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image dimensions too small");
    }
}
