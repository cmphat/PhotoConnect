package com.photoconnect.service;

import com.photoconnect.dto.DeliveredPhotoDto;
import com.photoconnect.dto.DeliveryAlbumDto;
import org.springframework.web.multipart.MultipartFile;

public interface DeliveryService {

    /**
     * Retrieves or creates a delivery album for an eligible booking, enforcing photographer ownership.
     */
    DeliveryAlbumDto getOrCreateAlbumForPhotographer(Long bookingId, Long photographerUserId);

    /**
     * Uploads and attaches a finished photograph to the booking's delivery album.
     */
    DeliveredPhotoDto addPhoto(Long bookingId, Long photographerUserId, MultipartFile file);

    /**
     * Deletes a photograph from the delivery album and cleans up the remote Cloudinary asset.
     */
    void removePhoto(Long bookingId, Long photoId, Long photographerUserId);

    /**
     * Submits the delivery album for customer review and advances booking state to DELIVERED.
     */
    DeliveryAlbumDto deliverAlbum(Long bookingId, Long photographerUserId, String notes);

    /**
     * Retrieves the delivery album for the customer of the booking, asserting customer ownership.
     */
    DeliveryAlbumDto getAlbumForCustomer(Long bookingId, Long customerUserId);

    /**
     * Customer confirms delivery receipt, accepting the album and transitioning booking to COMPLETED.
     */
    DeliveryAlbumDto confirmDeliveryReceipt(Long bookingId, Long customerUserId);

    /**
     * Customer requests a revision, moving album to REVISION_REQUESTED and booking to POST_PROCESSING.
     */
    DeliveryAlbumDto requestRevision(Long bookingId, Long customerUserId, String revisionNotes);

    /**
     * Authorizes and returns the secure download URL for an album photograph.
     */
    String getAuthorizedDownloadUrl(Long bookingId, Long photoId, Long currentUserId);
}
