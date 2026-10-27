package com.photoconnect.service;

import com.photoconnect.dto.DeliveredPhotoDto;
import com.photoconnect.dto.DeliveryAlbumDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.ResourceNotFoundException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DeliveredPhotoRepository;
import com.photoconnect.repository.DeliveryAlbumRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DeliveryAlbumRepository deliveryAlbumRepository;

    @Mock
    private DeliveredPhotoRepository deliveredPhotoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    @Mock
    private BookingStateMachineService bookingStateMachineService;

    private DeliveryServiceImpl deliveryService;

    private User photographerUser;
    private User customerUser;
    private User strangerUser;
    private PhotographerProfile photographerProfile;
    private Booking booking;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryServiceImpl(
                bookingRepository,
                deliveryAlbumRepository,
                deliveredPhotoRepository,
                userRepository,
                cloudinaryStorageService,
                bookingStateMachineService
        );

        photographerUser = new User();
        photographerUser.setId(10L);
        photographerUser.setFullName("Linh Nguyen");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setId(100L);
        photographerProfile.setUser(photographerUser);

        customerUser = new User();
        customerUser.setId(20L);
        customerUser.setFullName("Minh Tran");
        customerUser.setRole(UserRole.CUSTOMER);

        strangerUser = new User();
        strangerUser.setId(99L);
        strangerUser.setFullName("Hacker Bob");
        strangerUser.setRole(UserRole.CUSTOMER);

        booking = new Booking();
        booking.setId(500L);
        booking.setCustomer(customerUser);
        booking.setPhotographerProfile(photographerProfile);
        booking.setBookingDate(LocalDate.of(2026, 11, 1));
        booking.setBookingTime(LocalTime.of(10, 0));
        booking.setLocation("District 1, HCMC");
        booking.setAgreedPrice(BigDecimal.valueOf(1500000));
        booking.setDurationMinutes(120);
        booking.setEditedPhotoCount(25);
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Photographer creates new DRAFT delivery album if none exists")
    void getOrCreateAlbumForPhotographer_CreatesNew() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(deliveryAlbumRepository.findByBookingIdWithPhotos(500L)).thenReturn(Optional.empty());
        when(deliveryAlbumRepository.save(any(DeliveryAlbum.class))).thenAnswer(inv -> {
            DeliveryAlbum a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        DeliveryAlbumDto dto = deliveryService.getOrCreateAlbumForPhotographer(500L, 10L);

        assertThat(dto).isNotNull();
        assertThat(dto.bookingId()).isEqualTo(500L);
        assertThat(dto.status()).isEqualTo(DeliveryAlbumStatus.DRAFT);
        verify(deliveryAlbumRepository).save(any(DeliveryAlbum.class));
    }

    @Test
    @DisplayName("Non-photographer caller cannot access photographer delivery studio")
    void getOrCreateAlbumForPhotographer_Unauthorized() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> deliveryService.getOrCreateAlbumForPhotographer(500L, 99L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You are not the photographer");
    }

    private static byte[] createValidImageBytes(int width, int height, String format) {
        try {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, format, baos);
            return baos.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Photographer successfully uploads photo to delivery album")
    void addPhoto_Success() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        MockMultipartFile file = new MockMultipartFile(
                "photo", "portrait.jpg", "image/jpeg", createValidImageBytes(100, 100, "jpeg")
        );

        when(cloudinaryStorageService.uploadPrivateImage(eq(file), eq("photoconnect/delivery/booking_500")))
                .thenReturn(new CloudinaryStorageService.CloudinaryUploadResult("https://res.cloudinary.com/test/p1.jpg", "deliv_p1"));

        when(deliveredPhotoRepository.save(any(DeliveredPhoto.class))).thenAnswer(inv -> {
            DeliveredPhoto p = inv.getArgument(0);
            p.setId(101L);
            return p;
        });

        DeliveredPhotoDto result = deliveryService.addPhoto(500L, 10L, file);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(101L);
        assertThat(result.cloudinaryPublicId()).isEqualTo("deliv_p1");
        assertThat(result.secureUrl()).isNull(); // Direct storage URL is safely redacted in client DTO
        verify(deliveredPhotoRepository).save(any(DeliveredPhoto.class));
    }

    @Test
    @DisplayName("Uploading invalid file format is rejected before Cloudinary call")
    void addPhoto_InvalidMimeType_Rejected() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        MockMultipartFile badFile = new MockMultipartFile(
                "photo", "script.sh", "application/x-sh", new byte[]{1, 2}
        );

        assertThatThrownBy(() -> deliveryService.addPhoto(500L, 10L, badFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file format");

        verifyNoInteractions(cloudinaryStorageService);
    }

    @Test
    @DisplayName("Database save failure rolls back Cloudinary uploaded asset")
    void addPhoto_DbFailure_RollsBackCloudinary() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        MockMultipartFile file = new MockMultipartFile(
                "photo", "pic.jpg", "image/jpeg", createValidImageBytes(100, 100, "jpeg")
        );

        when(cloudinaryStorageService.uploadPrivateImage(any(), any()))
                .thenReturn(new CloudinaryStorageService.CloudinaryUploadResult("https://res.cloudinary.com/test/p1.jpg", "deliv_p1"));

        when(deliveredPhotoRepository.save(any(DeliveredPhoto.class)))
                .thenThrow(new RuntimeException("Simulated SQL constraint violation"));

        assertThatThrownBy(() -> deliveryService.addPhoto(500L, 10L, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to save delivered photo to database");

        verify(cloudinaryStorageService).deleteImage("deliv_p1");
    }

    @Test
    @DisplayName("Photographer removes photo from album and deletes Cloudinary asset")
    void removePhoto_Success() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://url", "pic.jpg", 100L);
        photo.setId(101L);
        album.addPhoto(photo);

        when(deliveredPhotoRepository.findByIdAndAlbumId(101L, 1L)).thenReturn(Optional.of(photo));

        deliveryService.removePhoto(500L, 101L, 10L);

        verify(cloudinaryStorageService).deleteImage("deliv_p1");
        verify(deliveredPhotoRepository).delete(photo);
    }

    @Test
    @DisplayName("Photographer delivers album and advances booking to DELIVERED status")
    void deliverAlbum_Success() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://url", "pic.jpg", 100L);
        album.addPhoto(photo);

        when(deliveryAlbumRepository.findByBookingIdWithPhotos(500L)).thenReturn(Optional.of(album));
        when(deliveryAlbumRepository.save(any(DeliveryAlbum.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryAlbumDto delivered = deliveryService.deliverAlbum(500L, 10L, "Enjoy your photos!");

        assertThat(delivered.status()).isEqualTo(DeliveryAlbumStatus.DELIVERED);
        assertThat(delivered.notes()).isEqualTo("Enjoy your photos!");
        verify(bookingStateMachineService).transition(500L, BookingStatus.DELIVERED, 10L, "Enjoy your photos!");
    }

    @Test
    @DisplayName("Empty album cannot be delivered to customer")
    void deliverAlbum_Empty_Throws() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setPhotos(new ArrayList<>());
        when(deliveryAlbumRepository.findByBookingIdWithPhotos(500L)).thenReturn(Optional.of(album));

        assertThatThrownBy(() -> deliveryService.deliverAlbum(500L, 10L, "Note"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot deliver an empty album");
    }

    @Test
    @DisplayName("Customer successfully views delivered album")
    void getAlbumForCustomer_Success() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(customerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);
        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://url", "pic.jpg", 100L);
        album.addPhoto(photo);

        when(deliveryAlbumRepository.findByBookingIdWithPhotos(500L)).thenReturn(Optional.of(album));

        DeliveryAlbumDto dto = deliveryService.getAlbumForCustomer(500L, 20L);

        assertThat(dto).isNotNull();
        assertThat(dto.status()).isEqualTo(DeliveryAlbumStatus.DELIVERED);
        assertThat(dto.photos()).hasSize(1);
    }

    @Test
    @DisplayName("Customer cannot view album that is still in DRAFT preparation")
    void getAlbumForCustomer_Draft_Throws() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(customerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DRAFT);

        when(deliveryAlbumRepository.findByBookingIdWithPhotos(500L)).thenReturn(Optional.of(album));

        assertThatThrownBy(() -> deliveryService.getAlbumForCustomer(500L, 20L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("still preparing the delivery album");
    }

    @Test
    @DisplayName("Customer confirms receipt, accepting album and advancing booking to COMPLETED")
    void confirmDeliveryReceipt_Success() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);

        when(deliveryAlbumRepository.findByBookingIdWithPhotos(500L)).thenReturn(Optional.of(album));
        when(deliveryAlbumRepository.save(any(DeliveryAlbum.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryAlbumDto accepted = deliveryService.confirmDeliveryReceipt(500L, 20L);

        assertThat(accepted.status()).isEqualTo(DeliveryAlbumStatus.ACCEPTED);
        verify(bookingStateMachineService).transition(500L, BookingStatus.COMPLETED, 20L, "Customer confirmed delivery receipt.");
    }

    @Test
    @DisplayName("Customer requests revision, transitioning album and booking to POST_PROCESSING")
    void requestRevision_Success() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);

        when(deliveryAlbumRepository.findByBookingIdWithPhotos(500L)).thenReturn(Optional.of(album));
        when(deliveryAlbumRepository.save(any(DeliveryAlbum.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryAlbumDto result = deliveryService.requestRevision(500L, 20L, "Please adjust the lighting on photo 3.");

        assertThat(result.status()).isEqualTo(DeliveryAlbumStatus.REVISION_REQUESTED);
        verify(bookingStateMachineService).transition(eq(500L), eq(BookingStatus.POST_PROCESSING), eq(20L), any());
    }

    @Test
    @DisplayName("Authorized customer gets download URL for photo")
    void getAuthorizedDownloadUrl_CustomerSuccess() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(customerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);

        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "sample.jpg", 200L);
        photo.setId(101L);

        when(deliveredPhotoRepository.findByIdAndAlbumId(101L, 1L)).thenReturn(Optional.of(photo));
        when(cloudinaryStorageService.generateAuthorizedUrl(eq("deliv_p1"), eq("upload"), eq("fl_attachment"), eq(300L)))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/s--signed--/fl_attachment/deliv_p1.jpg?__cld_token__=123");

        String downloadUrl = deliveryService.getAuthorizedDownloadUrl(500L, 101L, 20L);

        assertThat(downloadUrl).contains("s--signed--");
        verify(cloudinaryStorageService).generateAuthorizedUrl("deliv_p1", "upload", "fl_attachment", 300L);
    }

    @Test
    @DisplayName("Authorized customer gets short-lived preview URL for photo")
    void getAuthorizedPreviewUrl_CustomerSuccess() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(customerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "sample.jpg", 200L);
        photo.setId(101L);
        when(deliveredPhotoRepository.findByIdAndAlbumId(101L, 1L)).thenReturn(Optional.of(photo));

        when(cloudinaryStorageService.generateAuthorizedUrl(eq("deliv_p1"), eq("upload"), eq("c_limit,w_1600,h_1600,q_auto,f_auto"), eq(300L)))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/s--signed-preview--/sample.jpg");

        String previewUrl = deliveryService.getAuthorizedPreviewUrl(500L, 101L, 20L);

        assertThat(previewUrl).isEqualTo("https://res.cloudinary.com/demo/image/upload/s--signed-preview--/sample.jpg");
    }

    @Test
    @DisplayName("Authorized customer gets short-lived thumbnail URL for photo")
    void getAuthorizedThumbnailUrl_CustomerSuccess() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(customerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "sample.jpg", 200L);
        photo.setId(101L);
        when(deliveredPhotoRepository.findByIdAndAlbumId(101L, 1L)).thenReturn(Optional.of(photo));

        when(cloudinaryStorageService.generateAuthorizedUrl(eq("deliv_p1"), eq("upload"), eq("c_fill,w_500,h_500,q_auto,f_auto"), eq(300L)))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/s--signed-thumb--/sample.jpg");

        String thumbUrl = deliveryService.getAuthorizedThumbnailUrl(500L, 101L, 20L);

        assertThat(thumbUrl).isEqualTo("https://res.cloudinary.com/demo/image/upload/s--signed-thumb--/sample.jpg");
    }

    @Test
    @DisplayName("Customer cannot access photo preview while album is in DRAFT")
    void getAuthorizedPreviewUrl_DraftAlbum_CustomerBlocked() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(customerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DRAFT);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        assertThatThrownBy(() -> deliveryService.getAuthorizedPreviewUrl(500L, 101L, 20L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not yet published for customer access");
    }

    @Test
    @DisplayName("Assigned photographer can access photo preview even when album is in DRAFT")
    void getAuthorizedPreviewUrl_DraftAlbum_PhotographerAllowed() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(photographerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DRAFT);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "sample.jpg", 200L);
        photo.setId(101L);
        when(deliveredPhotoRepository.findByIdAndAlbumId(101L, 1L)).thenReturn(Optional.of(photo));

        when(cloudinaryStorageService.generateAuthorizedUrl(eq("deliv_p1"), eq("upload"), anyString(), eq(300L)))
                .thenReturn("https://res.cloudinary.com/demo/signed-photographer-preview.jpg");

        String previewUrl = deliveryService.getAuthorizedPreviewUrl(500L, 101L, 10L);

        assertThat(previewUrl).isEqualTo("https://res.cloudinary.com/demo/signed-photographer-preview.jpg");
    }

    @Test
    @DisplayName("Admin can access photo download for any booking")
    void getAuthorizedDownloadUrl_AdminSuccess() {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(UserRole.ADMIN);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "sample.jpg", 200L);
        photo.setId(101L);
        when(deliveredPhotoRepository.findByIdAndAlbumId(101L, 1L)).thenReturn(Optional.of(photo));

        when(cloudinaryStorageService.generateAuthorizedUrl(eq("deliv_p1"), eq("upload"), eq("fl_attachment"), eq(300L)))
                .thenReturn("https://res.cloudinary.com/demo/admin-signed-download.jpg");

        String downloadUrl = deliveryService.getAuthorizedDownloadUrl(500L, 101L, 1L);

        assertThat(downloadUrl).isEqualTo("https://res.cloudinary.com/demo/admin-signed-download.jpg");
    }

    @Test
    @DisplayName("Unauthorized stranger is blocked from downloading booking photos")
    void getAuthorizedDownloadUrl_StrangerBlocked() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(99L)).thenReturn(Optional.of(strangerUser));

        assertThatThrownBy(() -> deliveryService.getAuthorizedDownloadUrl(500L, 101L, 99L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You are not authorized");
    }

    @Test
    @DisplayName("Manager cannot access private delivery photographs by role")
    void getAuthorizedDownloadUrl_ManagerBlocked() {
        User manager = new User();
        manager.setId(77L);
        manager.setRole(UserRole.MANAGER);
        manager.setStatus(UserStatus.ACTIVE);
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(77L)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> deliveryService.getAuthorizedDownloadUrl(500L, 101L, 77L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not authorized");

        verifyNoInteractions(deliveryAlbumRepository, deliveredPhotoRepository, cloudinaryStorageService);
    }

    @Test
    @DisplayName("Safe Migration: migratePhotoToAuthenticated updates URL only after Cloudinary confirms rename")
    void migratePhotoToAuthenticated_Success() {
        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        DeliveredPhoto photo = new DeliveredPhoto(album, "booking_500/photo1",
                "https://res.cloudinary.com/demo/image/upload/v1234/booking_500/photo1.jpg", "photo1.jpg", 500L);
        photo.setId(201L);

        when(deliveredPhotoRepository.findById(201L)).thenReturn(Optional.of(photo));
        when(cloudinaryStorageService.migrateAssetToAuthenticated("booking_500/photo1")).thenReturn(true);
        when(deliveredPhotoRepository.save(any(DeliveredPhoto.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = deliveryService.migratePhotoToAuthenticated(201L);

        assertThat(result).isTrue();
        assertThat(photo.getSecureUrl()).contains("/image/authenticated/");
        assertThat(photo.getDeliveryType()).isEqualTo("authenticated");
        verify(deliveredPhotoRepository).save(photo);
    }

    @Test
    @DisplayName("Safe Migration: Cloudinary failure aborts migration without deleting or modifying database record")
    void migratePhotoToAuthenticated_FailurePreservesRecord() {
        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        DeliveredPhoto photo = new DeliveredPhoto(album, "booking_500/photo1",
                "https://res.cloudinary.com/demo/image/upload/v1234/booking_500/photo1.jpg", "photo1.jpg", 500L);
        photo.setId(201L);

        when(deliveredPhotoRepository.findById(201L)).thenReturn(Optional.of(photo));
        when(cloudinaryStorageService.migrateAssetToAuthenticated("booking_500/photo1")).thenReturn(false);

        boolean result = deliveryService.migratePhotoToAuthenticated(201L);

        assertThat(result).isFalse();
        assertThat(photo.getSecureUrl()).contains("/image/upload/"); // Untouched
        verify(deliveredPhotoRepository, never()).save(any());
        verify(deliveredPhotoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Fail Closed: getAuthorizedDownloadUrl throws IllegalStateException when signed URL generation fails")
    void getAuthorizedDownloadUrl_GenerationFails_ThrowsIllegalStateException() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(customerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "sample.jpg", 200L);
        photo.setId(101L);
        when(deliveredPhotoRepository.findByIdAndAlbumId(101L, 1L)).thenReturn(Optional.of(photo));
        when(cloudinaryStorageService.generateAuthorizedUrl(any(), any(), any(), anyLong())).thenReturn(null);

        assertThatThrownBy(() -> deliveryService.getAuthorizedDownloadUrl(500L, 101L, 20L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to generate authorized download link");
    }

    @Test
    @DisplayName("Fail Closed: getAuthorizedPreviewUrl throws IllegalStateException when signed URL generation fails")
    void getAuthorizedPreviewUrl_GenerationFails_ThrowsIllegalStateException() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(customerUser));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        album.setStatus(DeliveryAlbumStatus.DELIVERED);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        DeliveredPhoto photo = new DeliveredPhoto(album, "deliv_p1", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "sample.jpg", 200L);
        photo.setId(101L);
        when(deliveredPhotoRepository.findByIdAndAlbumId(101L, 1L)).thenReturn(Optional.of(photo));
        when(cloudinaryStorageService.generateAuthorizedUrl(any(), any(), any(), anyLong())).thenReturn("");

        assertThatThrownBy(() -> deliveryService.getAuthorizedPreviewUrl(500L, 101L, 20L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to generate authorized preview link");
    }
}
