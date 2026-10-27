package com.photoconnect.dto;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Safe representation of a Booking for viewing in the UI.
 * Does not expose sensitive user data.
 */
public class BookingViewDto {

    private Long id;
    private String customerName;
    private String photographerName;
    private String sessionTitle;
    private Long photographerProfileId;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String location;
    private String notes;
    private BigDecimal agreedPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public BookingViewDto() {
    }

    public static BookingViewDto from(Booking booking) {
        BookingViewDto dto = new BookingViewDto();
        dto.setId(booking.getId());
        dto.setCustomerName(booking.getCustomer().getFullName());
        String displayName = booking.getPhotographerProfile().getDisplayName();
        dto.setPhotographerName(displayName == null || displayName.isBlank()
                ? booking.getPhotographerProfile().getUser().getFullName()
                : displayName);
        dto.setSessionTitle("Photography Session");
        dto.setPhotographerProfileId(booking.getPhotographerProfile().getId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setBookingTime(booking.getBookingTime());
        dto.setLocation(booking.getLocation());
        dto.setNotes(booking.getNotes());
        dto.setAgreedPrice(booking.getAgreedPrice());
        dto.setStatus(booking.getStatus());
        dto.setPackageName(booking.getPackageName() != null ? booking.getPackageName() : "Custom Session");
        dto.setDurationMinutes(booking.getDurationMinutes() != null ? booking.getDurationMinutes() : 120);
        dto.setEditedPhotoCount(booking.getEditedPhotoCount());
        dto.setDeliveryDays(booking.getDeliveryDays());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setCancellationReason(booking.getCancellationReason());
        if (booking.getCancelledBy() != null) {
            dto.setCancelledByName(booking.getCancelledBy().getFullName());
        }
        dto.setCancelledAt(booking.getCancelledAt());
        dto.setConceptDescription(booking.getConceptDescription());
        dto.setMoodboardUrl(booking.getMoodboardUrl());
        dto.setBackupLocation(booking.getBackupLocation());
        dto.setOutfitCount(booking.getOutfitCount());
        dto.setShotList(booking.getShotList());
        dto.setPropsNeeded(booking.getPropsNeeded());
        dto.setParticipantCount(booking.getParticipantCount());
        dto.setPhotographerNotes(booking.getPhotographerNotes());
        dto.setBasePrice(booking.getBasePrice() != null ? booking.getBasePrice() : booking.getAgreedPrice());
        dto.setNightSurcharge(booking.getNightSurcharge() != null ? booking.getNightSurcharge() : BigDecimal.ZERO);
        dto.setTravelDistanceKm(booking.getTravelDistanceKm() != null ? booking.getTravelDistanceKm() : 0);
        dto.setTravelSurcharge(booking.getTravelSurcharge() != null ? booking.getTravelSurcharge() : BigDecimal.ZERO);
        dto.setExtrasFee(booking.getExtrasFee() != null ? booking.getExtrasFee() : BigDecimal.ZERO);
        dto.setExtraHours(booking.getExtraHours() != null ? booking.getExtraHours() : 0);
        dto.setExtraPhotos(booking.getExtraPhotos() != null ? booking.getExtraPhotos() : 0);
        dto.setAdditionalPhotographer(Boolean.TRUE.equals(booking.getAdditionalPhotographer()));
        return dto;
    }

    private String cancellationReason;
    private String cancelledByName;
    private LocalDateTime cancelledAt;

    private String packageName;
    private Integer durationMinutes;
    private Integer editedPhotoCount;
    private Integer deliveryDays;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getEditedPhotoCount() {
        return editedPhotoCount;
    }

    public void setEditedPhotoCount(Integer editedPhotoCount) {
        this.editedPhotoCount = editedPhotoCount;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    public String getBadgeClass() {
        return status != null ? status.getBadgeClass() : "pc-badge-secondary";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhotographerName() {
        return photographerName;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
    }

    public Long getPhotographerProfileId() {
        return photographerProfileId;
    }

    public void setPhotographerProfileId(Long photographerProfileId) {
        this.photographerProfileId = photographerProfileId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancelledByName() {
        return cancelledByName;
    }

    public void setCancelledByName(String cancelledByName) {
        this.cancelledByName = cancelledByName;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    private String conceptDescription;
    private String moodboardUrl;
    private String backupLocation;
    private Integer outfitCount;
    private String shotList;
    private String propsNeeded;
    private Integer participantCount;

    public String getConceptDescription() {
        return conceptDescription;
    }

    public void setConceptDescription(String conceptDescription) {
        this.conceptDescription = conceptDescription;
    }

    public String getMoodboardUrl() {
        return moodboardUrl;
    }

    public void setMoodboardUrl(String moodboardUrl) {
        this.moodboardUrl = moodboardUrl;
    }

    public String getBackupLocation() {
        return backupLocation;
    }

    public void setBackupLocation(String backupLocation) {
        this.backupLocation = backupLocation;
    }

    public Integer getOutfitCount() {
        return outfitCount;
    }

    public void setOutfitCount(Integer outfitCount) {
        this.outfitCount = outfitCount;
    }

    public String getShotList() {
        return shotList;
    }

    public void setShotList(String shotList) {
        this.shotList = shotList;
    }

    public String getPropsNeeded() {
        return propsNeeded;
    }

    public void setPropsNeeded(String propsNeeded) {
        this.propsNeeded = propsNeeded;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    private String photographerNotes;
    private Long pastCompletedShootsWithClient;

    public String getPhotographerNotes() {
        return photographerNotes;
    }

    public void setPhotographerNotes(String photographerNotes) {
        this.photographerNotes = photographerNotes;
    }

    public Long getPastCompletedShootsWithClient() {
        return pastCompletedShootsWithClient;
    }

    public void setPastCompletedShootsWithClient(Long pastCompletedShootsWithClient) {
        this.pastCompletedShootsWithClient = pastCompletedShootsWithClient;
    }

    private BigDecimal basePrice;
    private BigDecimal nightSurcharge;
    private Integer travelDistanceKm;
    private BigDecimal travelSurcharge;
    private BigDecimal extrasFee;
    private Integer extraHours;
    private Integer extraPhotos;
    private Boolean additionalPhotographer;

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getNightSurcharge() {
        return nightSurcharge;
    }

    public void setNightSurcharge(BigDecimal nightSurcharge) {
        this.nightSurcharge = nightSurcharge;
    }

    public Integer getTravelDistanceKm() {
        return travelDistanceKm;
    }

    public void setTravelDistanceKm(Integer travelDistanceKm) {
        this.travelDistanceKm = travelDistanceKm;
    }

    public BigDecimal getTravelSurcharge() {
        return travelSurcharge;
    }

    public void setTravelSurcharge(BigDecimal travelSurcharge) {
        this.travelSurcharge = travelSurcharge;
    }

    public BigDecimal getExtrasFee() {
        return extrasFee;
    }

    public void setExtrasFee(BigDecimal extrasFee) {
        this.extrasFee = extrasFee;
    }

    public Integer getExtraHours() {
        return extraHours;
    }

    public void setExtraHours(Integer extraHours) {
        this.extraHours = extraHours;
    }

    public Integer getExtraPhotos() {
        return extraPhotos;
    }

    public void setExtraPhotos(Integer extraPhotos) {
        this.extraPhotos = extraPhotos;
    }

    public Boolean getAdditionalPhotographer() {
        return additionalPhotographer;
    }

    public void setAdditionalPhotographer(Boolean additionalPhotographer) {
        this.additionalPhotographer = additionalPhotographer;
    }
}
