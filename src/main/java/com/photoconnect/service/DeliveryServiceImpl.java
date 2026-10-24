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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    private static final String CLOUDINARY_FOLDER = "photoconnect/delivery";
    private static final long MAX_FILE_SIZE_BYTES = 15L * 1024 * 1024; // 15 MB for high-res deliverables
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final BookingRepository bookingRepository;
    private final DeliveryAlbumRepository deliveryAlbumRepository;
    private final DeliveredPhotoRepository deliveredPhotoRepository;
    private final UserRepository userRepository;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final BookingStateMachineService bookingStateMachineService;
    private final NotificationService notificationService;

    @org.springframework.beans.factory.annotation.Autowired
    public DeliveryServiceImpl(BookingRepository bookingRepository,
                               DeliveryAlbumRepository deliveryAlbumRepository,
                               DeliveredPhotoRepository deliveredPhotoRepository,
                               UserRepository userRepository,
                               CloudinaryStorageService cloudinaryStorageService,
                               BookingStateMachineService bookingStateMachineService,
                               @org.springframework.beans.factory.annotation.Autowired(required = false) NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.deliveryAlbumRepository = deliveryAlbumRepository;
        this.deliveredPhotoRepository = deliveredPhotoRepository;
        this.userRepository = userRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
        this.bookingStateMachineService = bookingStateMachineService;
        this.notificationService = notificationService;
    }

    public DeliveryServiceImpl(BookingRepository bookingRepository,
                               DeliveryAlbumRepository deliveryAlbumRepository,
                               DeliveredPhotoRepository deliveredPhotoRepository,
                               UserRepository userRepository,
                               CloudinaryStorageService cloudinaryStorageService,
                               BookingStateMachineService bookingStateMachineService) {
        this(bookingRepository, deliveryAlbumRepository, deliveredPhotoRepository, userRepository, cloudinaryStorageService, bookingStateMachineService, null);
    }

    @Override
    @Transactional
    public DeliveryAlbumDto getOrCreateAlbumForPhotographer(Long bookingId, Long photographerUserId) {
        Booking booking = requirePhotographerBooking(bookingId, photographerUserId);

        DeliveryAlbum album = deliveryAlbumRepository.findByBookingIdWithPhotos(bookingId)
                .orElseGet(() -> {
                    DeliveryAlbum newAlbum = new DeliveryAlbum(booking);
                    return deliveryAlbumRepository.save(newAlbum);
                });

        return DeliveryAlbumDto.fromEntity(album);
    }

    @Override
    @Transactional
    public DeliveredPhotoDto addPhoto(Long bookingId, Long photographerUserId, MultipartFile file) {
        Booking booking = requirePhotographerBooking(bookingId, photographerUserId);
        validateFile(file);

        DeliveryAlbum album = deliveryAlbumRepository.findByBookingId(bookingId)
                .orElseGet(() -> deliveryAlbumRepository.save(new DeliveryAlbum(booking)));

        if (album.getStatus() == DeliveryAlbumStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot add photos to an already accepted delivery album.");
        }

        // Upload to Cloudinary under photoconnect/delivery/booking_{id}
        String folder = CLOUDINARY_FOLDER + "/booking_" + bookingId;
        CloudinaryStorageService.CloudinaryUploadResult uploadResult =
                cloudinaryStorageService.uploadImage(file, folder);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.length() > 250) {
            originalFilename = originalFilename.substring(0, 250);
        }

        DeliveredPhoto photo = new DeliveredPhoto(
                album,
                uploadResult.publicId(),
                uploadResult.secureUrl(),
                originalFilename,
                file.getSize()
        );

        try {
            DeliveredPhoto savedPhoto = deliveredPhotoRepository.save(photo);
            album.addPhoto(savedPhoto);
            log.info("Uploaded delivered photo [{}] for booking [{}] album [{}]",
                    savedPhoto.getId(), bookingId, album.getId());
            return DeliveredPhotoDto.fromEntity(savedPhoto);
        } catch (Exception dbEx) {
            log.warn("DB save failed for delivered photo. Rolling back Cloudinary asset [{}]: {}",
                    uploadResult.publicId(), dbEx.getMessage());
            try {
                cloudinaryStorageService.deleteImage(uploadResult.publicId());
            } catch (Exception rollbackEx) {
                log.error("Failed Cloudinary rollback for asset [{}]: {}",
                        uploadResult.publicId(), rollbackEx.getMessage());
            }
            throw new RuntimeException("Failed to save delivered photo to database.", dbEx);
        }
    }

    @Override
    @Transactional
    public void removePhoto(Long bookingId, Long photoId, Long photographerUserId) {
        requirePhotographerBooking(bookingId, photographerUserId);

        DeliveryAlbum album = deliveryAlbumRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery album not found for booking: " + bookingId));

        if (album.getStatus() == DeliveryAlbumStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot remove photos from an already accepted delivery album.");
        }

        DeliveredPhoto photo = deliveredPhotoRepository.findByIdAndAlbumId(photoId, album.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found in this album: " + photoId));

        String publicId = photo.getCloudinaryPublicId();
        cloudinaryStorageService.deleteImage(publicId);

        album.removePhoto(photo);
        deliveredPhotoRepository.delete(photo);
        log.info("Deleted photo [{}] from album [{}] booking [{}]", photoId, album.getId(), bookingId);
    }

    @Override
    @Transactional
    public DeliveryAlbumDto deliverAlbum(Long bookingId, Long photographerUserId, String notes) {
        requirePhotographerBooking(bookingId, photographerUserId);

        DeliveryAlbum album = deliveryAlbumRepository.findByBookingIdWithPhotos(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery album not found for booking: " + bookingId));

        if (album.getPhotos() == null || album.getPhotos().isEmpty()) {
            throw new IllegalStateException("Cannot deliver an empty album. Please upload at least one photograph.");
        }

        if (album.getStatus() == DeliveryAlbumStatus.ACCEPTED) {
            throw new IllegalStateException("Album is already accepted by client.");
        }

        album.setStatus(DeliveryAlbumStatus.DELIVERED);
        album.setDeliveredAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            album.setNotes(notes.trim());
        }
        DeliveryAlbum savedAlbum = deliveryAlbumRepository.save(album);

        // Advance booking status to DELIVERED via state machine
        bookingStateMachineService.transition(
                bookingId,
                BookingStatus.DELIVERED,
                photographerUserId,
                notes != null && !notes.isBlank() ? notes.trim() : "Photographer delivered finished photos album."
        );

        if (notificationService != null && album.getBooking() != null && album.getBooking().getCustomer() != null) {
            notificationService.sendNotification(
                    album.getBooking().getCustomer().getId(),
                    NotificationType.DELIVERY_READY,
                    "Finished Photos Delivered",
                    "Your photographer has delivered your finished photo gallery. Preview and download them now!",
                    bookingId,
                    "/bookings/" + bookingId + "/delivery"
            );
        }

        log.info("Delivered album [{}] for booking [{}] by photographer [{}]",
                savedAlbum.getId(), bookingId, photographerUserId);

        return DeliveryAlbumDto.fromEntity(savedAlbum);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryAlbumDto getAlbumForCustomer(Long bookingId, Long customerUserId) {
        Booking booking = requireCustomerOrAdminBooking(bookingId, customerUserId);

        DeliveryAlbum album = deliveryAlbumRepository.findByBookingIdWithPhotos(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery album has been published for this booking yet."));

        // If album is still in DRAFT, customer should not preview until delivered
        if (album.getStatus() == DeliveryAlbumStatus.DRAFT) {
            throw new IllegalStateException("Your photographer is still preparing the delivery album. It is not ready yet.");
        }

        return DeliveryAlbumDto.fromEntity(album);
    }

    @Override
    @Transactional
    public DeliveryAlbumDto confirmDeliveryReceipt(Long bookingId, Long customerUserId) {
        Booking booking = requireCustomerBooking(bookingId, customerUserId);

        DeliveryAlbum album = deliveryAlbumRepository.findByBookingIdWithPhotos(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery album not found for booking: " + bookingId));

        if (album.getStatus() == DeliveryAlbumStatus.ACCEPTED) {
            return DeliveryAlbumDto.fromEntity(album); // Idempotent
        }

        if (album.getStatus() != DeliveryAlbumStatus.DELIVERED) {
            throw new IllegalStateException("Cannot accept album in status: " + album.getStatus());
        }

        album.setStatus(DeliveryAlbumStatus.ACCEPTED);
        album.setAcceptedAt(LocalDateTime.now());
        DeliveryAlbum savedAlbum = deliveryAlbumRepository.save(album);

        // Advance booking status to COMPLETED via state machine
        bookingStateMachineService.transition(
                bookingId,
                BookingStatus.COMPLETED,
                customerUserId,
                "Customer confirmed delivery receipt."
        );

        if (notificationService != null && booking.getPhotographerProfile() != null &&
            booking.getPhotographerProfile().getUser() != null) {
            notificationService.sendNotification(
                    booking.getPhotographerProfile().getUser().getId(),
                    NotificationType.DELIVERY_CONFIRMED,
                    "Delivery Confirmed",
                    "Client confirmed receipt and accepted the photo collection for booking #" + bookingId,
                    bookingId,
                    "/photographer/bookings/" + bookingId + "/delivery"
            );
        }

        log.info("Customer [{}] accepted delivery album [{}] for booking [{}]",
                customerUserId, savedAlbum.getId(), bookingId);

        return DeliveryAlbumDto.fromEntity(savedAlbum);
    }

    @Override
    @Transactional
    public DeliveryAlbumDto requestRevision(Long bookingId, Long customerUserId, String revisionNotes) {
        Booking booking = requireCustomerBooking(bookingId, customerUserId);

        if (revisionNotes == null || revisionNotes.isBlank()) {
            throw new IllegalArgumentException("Please provide details for the revision request.");
        }

        DeliveryAlbum album = deliveryAlbumRepository.findByBookingIdWithPhotos(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery album not found for booking: " + bookingId));

        if (album.getStatus() != DeliveryAlbumStatus.DELIVERED) {
            throw new IllegalStateException("Revisions can only be requested on delivered albums awaiting review.");
        }

        album.setStatus(DeliveryAlbumStatus.REVISION_REQUESTED);
        String updatedNotes = (album.getNotes() != null ? album.getNotes() + "\n" : "") +
                "[Revision Request]: " + revisionNotes.trim();
        if (updatedNotes.length() > 1000) {
            updatedNotes = updatedNotes.substring(0, 1000);
        }
        album.setNotes(updatedNotes);
        DeliveryAlbum savedAlbum = deliveryAlbumRepository.save(album);

        // Move booking back to POST_PROCESSING for photographer updates
        bookingStateMachineService.transition(
                bookingId,
                BookingStatus.POST_PROCESSING,
                customerUserId,
                "Client requested revisions: " + revisionNotes.trim()
        );

        if (notificationService != null && booking.getPhotographerProfile() != null &&
            booking.getPhotographerProfile().getUser() != null) {
            notificationService.sendNotification(
                    booking.getPhotographerProfile().getUser().getId(),
                    NotificationType.SCHEDULE_CHANGED,
                    "Revision Requested",
                    "Client requested adjustments: " + revisionNotes.trim(),
                    bookingId,
                    "/photographer/bookings/" + bookingId + "/delivery"
            );
        }

        log.info("Customer [{}] requested revision for album [{}] booking [{}]",
                customerUserId, savedAlbum.getId(), bookingId);

        return DeliveryAlbumDto.fromEntity(savedAlbum);
    }

    @Override
    @Transactional(readOnly = true)
    public String getAuthorizedDownloadUrl(Long bookingId, Long photoId, Long currentUserId) {
        if (currentUserId == null) {
            throw new UnauthorizedException("Authentication is required to download photos.");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found: " + bookingId));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("User not found: " + currentUserId));

        boolean isCustomer = booking.getCustomer() != null && booking.getCustomer().getId().equals(currentUserId);
        boolean isPhotographer = booking.getPhotographerProfile() != null &&
                                 booking.getPhotographerProfile().getUser() != null &&
                                 booking.getPhotographerProfile().getUser().getId().equals(currentUserId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isCustomer && !isPhotographer && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to download photos from this booking.");
        }

        DeliveryAlbum album = deliveryAlbumRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery album not found for booking: " + bookingId));

        // Customers cannot download if album is still in DRAFT
        if (isCustomer && album.getStatus() == DeliveryAlbumStatus.DRAFT) {
            throw new UnauthorizedException("This album is not yet published for customer download.");
        }

        DeliveredPhoto photo = deliveredPhotoRepository.findByIdAndAlbumId(photoId, album.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found in this album: " + photoId));

        return photo.getDownloadUrl();
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    private Booking requirePhotographerBooking(Long bookingId, Long photographerUserId) {
        if (bookingId == null || photographerUserId == null) {
            throw new IllegalArgumentException("Booking ID and photographer user ID are required.");
        }
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found: " + bookingId));

        boolean isPhotographer = booking.getPhotographerProfile() != null &&
                                 booking.getPhotographerProfile().getUser() != null &&
                                 booking.getPhotographerProfile().getUser().getId().equals(photographerUserId);

        if (!isPhotographer) {
            throw new UnauthorizedException("You are not the photographer for this booking.");
        }

        return booking;
    }

    private Booking requireCustomerBooking(Long bookingId, Long customerUserId) {
        if (bookingId == null || customerUserId == null) {
            throw new IllegalArgumentException("Booking ID and customer user ID are required.");
        }
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found: " + bookingId));

        boolean isCustomer = booking.getCustomer() != null && booking.getCustomer().getId().equals(customerUserId);
        if (!isCustomer) {
            throw new UnauthorizedException("You are not the customer for this booking.");
        }

        return booking;
    }

    private Booking requireCustomerOrAdminBooking(Long bookingId, Long userId) {
        if (bookingId == null || userId == null) {
            throw new IllegalArgumentException("Booking ID and user ID are required.");
        }
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found: " + bookingId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found: " + userId));

        boolean isCustomer = booking.getCustomer() != null && booking.getCustomer().getId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isCustomer && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to view this delivery gallery.");
        }

        return booking;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image file to upload.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds 15 MB limit. Please optimize the photo before uploading.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file format. Only JPEG, PNG, and WEBP images are supported.");
        }
    }
}
