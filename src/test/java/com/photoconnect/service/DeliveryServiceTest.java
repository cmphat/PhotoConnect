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

    @Test
    @DisplayName("Photographer successfully uploads photo to delivery album")
    void addPhoto_Success() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));

        DeliveryAlbum album = new DeliveryAlbum(booking);
        album.setId(1L);
        when(deliveryAlbumRepository.findByBookingId(500L)).thenReturn(Optional.of(album));

        MockMultipartFile file = new MockMultipartFile(
                "photo", "portrait.jpg", "image/jpeg", new byte[]{1, 2, 3, 4}
        );

        when(cloudinaryStorageService.uploadImage(eq(file), eq("photoconnect/delivery/booking_500")))
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
        assertThat(result.secureUrl()).isEqualTo("https://res.cloudinary.com/test/p1.jpg");
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
                "photo", "pic.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        when(cloudinaryStorageService.uploadImage(any(), any()))
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

        String downloadUrl = deliveryService.getAuthorizedDownloadUrl(500L, 101L, 20L);

        assertThat(downloadUrl).contains("fl_attachment");
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
}
