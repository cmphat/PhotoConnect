package com.photoconnect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a shoot booking request created by a customer for an approved photographer.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photographer_profile_id", nullable = false)
    private PhotographerProfile photographerProfile;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "photographer_notes", length = 1500)
    private String photographerNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_package_id")
    private ServicePackage servicePackage;

    @Column(name = "package_name", length = 150)
    private String packageName;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 120;

    @Column(name = "edited_photo_count")
    private Integer editedPhotoCount;

    @Column(name = "delivery_days")
    private Integer deliveryDays;

    @Column(name = "agreed_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal agreedPrice;

    @Column(name = "base_price", precision = 18, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "night_surcharge", precision = 18, scale = 2)
    private BigDecimal nightSurcharge = BigDecimal.ZERO;

    @Column(name = "travel_distance_km")
    private Integer travelDistanceKm = 0;

    @Column(name = "travel_surcharge", precision = 18, scale = 2)
    private BigDecimal travelSurcharge = BigDecimal.ZERO;

    @Column(name = "extras_fee", precision = 18, scale = 2)
    private BigDecimal extrasFee = BigDecimal.ZERO;

    @Column(name = "extra_hours")
    private Integer extraHours = 0;

    @Column(name = "extra_photos")
    private Integer extraPhotos = 0;

    @Column(name = "additional_photographer")
    private Boolean additionalPhotographer = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_user_id")
    private User cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "concept_description", length = 1000)
    private String conceptDescription;

    @Column(name = "moodboard_url", length = 500)
    private String moodboardUrl;

    @Column(name = "backup_location", length = 255)
    private String backupLocation;

    @Column(name = "outfit_count")
    private Integer outfitCount;

    @Column(name = "shot_list", length = 1500)
    private String shotList;

    @Column(name = "props_needed", length = 500)
    private String propsNeeded;

    @Column(name = "participant_count")
    private Integer participantCount;

    public Booking() {
    }

    public Booking(User customer,
                   PhotographerProfile photographerProfile,
                   LocalDate bookingDate,
                   LocalTime bookingTime,
                   String location,
                   String notes,
                   BigDecimal agreedPrice) {
        this.customer = customer;
        this.photographerProfile = photographerProfile;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.location = location;
        this.notes = notes;
        this.agreedPrice = agreedPrice;
        this.status = BookingStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public PhotographerProfile getPhotographerProfile() {
        return photographerProfile;
    }

    public void setPhotographerProfile(PhotographerProfile photographerProfile) {
        this.photographerProfile = photographerProfile;
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

    public String getPhotographerNotes() {
        return photographerNotes;
    }

    public void setPhotographerNotes(String photographerNotes) {
        this.photographerNotes = photographerNotes;
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

    public ServicePackage getServicePackage() {
        return servicePackage;
    }

    public void setServicePackage(ServicePackage servicePackage) {
        this.servicePackage = servicePackage;
    }

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
        this.durationMinutes = durationMinutes != null ? durationMinutes : 120;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public User getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(User cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

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
