package com.photoconnect.dto;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.DeliveryAlbum;
import com.photoconnect.entity.DeliveryAlbumStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record DeliveryAlbumDto(
        Long id,
        Long bookingId,
        BookingStatus bookingStatus,
        DeliveryAlbumStatus status,
        String statusDisplayName,
        String statusBadgeClass,
        String notes,
        LocalDateTime deliveredAt,
        LocalDateTime acceptedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<DeliveredPhotoDto> photos,
        Integer expectedPhotoCount,
        int actualPhotoCount,
        String packageName,
        String photographerName,
        String customerName,
        String location,
        LocalDate bookingDate
) {
    public static DeliveryAlbumDto fromEntity(DeliveryAlbum album) {
        if (album == null) {
            return null;
        }

        Booking booking = album.getBooking();
        List<DeliveredPhotoDto> photoDtos = album.getPhotos() != null
                ? album.getPhotos().stream().map(DeliveredPhotoDto::fromEntity).collect(Collectors.toList())
                : Collections.emptyList();

        String photogName = null;
        String custName = null;
        String loc = null;
        LocalDate bDate = null;
        String pkgName = null;
        Integer expectedCount = null;
        BookingStatus bStatus = null;

        if (booking != null) {
            bStatus = booking.getStatus();
            loc = booking.getLocation();
            bDate = booking.getBookingDate();
            pkgName = booking.getPackageName();
            expectedCount = booking.getEditedPhotoCount();
            if (booking.getPhotographerProfile() != null && booking.getPhotographerProfile().getUser() != null) {
                photogName = booking.getPhotographerProfile().getUser().getFullName();
            }
            if (booking.getCustomer() != null) {
                custName = booking.getCustomer().getFullName();
            }
        }

        DeliveryAlbumStatus st = album.getStatus();

        return new DeliveryAlbumDto(
                album.getId(),
                booking != null ? booking.getId() : null,
                bStatus,
                st,
                st != null ? st.getDisplayName() : "Unknown",
                st != null ? st.getBadgeClass() : "pc-badge-secondary",
                album.getNotes(),
                album.getDeliveredAt(),
                album.getAcceptedAt(),
                album.getCreatedAt(),
                album.getUpdatedAt(),
                photoDtos,
                expectedCount,
                photoDtos.size(),
                pkgName,
                photogName,
                custName,
                loc,
                bDate
        );
    }
}
