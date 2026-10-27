package com.photoconnect.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Form backing and request DTO for creating a photographer booking.
 */
public class BookingRequest {

    private Long photographerId;
    private Long packageId;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate bookingDate;

    @NotNull(message = "Booking time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime bookingTime;

    @NotBlank(message = "Shoot location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 1000, message = "Concept description cannot exceed 1000 characters")
    private String conceptDescription;

    @Size(max = 500, message = "Moodboard URL cannot exceed 500 characters")
    private String moodboardUrl;

    @Size(max = 255, message = "Backup location cannot exceed 255 characters")
    private String backupLocation;

    private Integer outfitCount;

    @Size(max = 1500, message = "Shot list cannot exceed 1500 characters")
    private String shotList;

    @Size(max = 500, message = "Props needed cannot exceed 500 characters")
    private String propsNeeded;

    private Integer participantCount;

    private Integer travelDistanceKm;
    private Boolean nightShoot;
    private Integer extraHours;
    private Integer extraPhotos;
    private Boolean additionalPhotographer;

    public BookingRequest() {
    }

    public BookingRequest(LocalDate bookingDate, LocalTime bookingTime, String location, String notes) {
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.location = location;
        this.notes = notes;
    }

    public BookingRequest(Long photographerId, LocalDate bookingDate, LocalTime bookingTime, String location, String notes) {
        this.photographerId = photographerId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.location = location;
        this.notes = notes;
    }

    public Long getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(Long photographerId) {
        this.photographerId = photographerId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
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
        this.location = location != null ? location.trim() : null;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes.trim() : null;
    }

    public String getConceptDescription() {
        return conceptDescription;
    }

    public void setConceptDescription(String conceptDescription) {
        this.conceptDescription = conceptDescription != null ? conceptDescription.trim() : null;
    }

    public String getMoodboardUrl() {
        return moodboardUrl;
    }

    public void setMoodboardUrl(String moodboardUrl) {
        this.moodboardUrl = moodboardUrl != null ? moodboardUrl.trim() : null;
    }

    public String getBackupLocation() {
        return backupLocation;
    }

    public void setBackupLocation(String backupLocation) {
        this.backupLocation = backupLocation != null ? backupLocation.trim() : null;
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
        this.shotList = shotList != null ? shotList.trim() : null;
    }

    public String getPropsNeeded() {
        return propsNeeded;
    }

    public void setPropsNeeded(String propsNeeded) {
        this.propsNeeded = propsNeeded != null ? propsNeeded.trim() : null;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public Integer getTravelDistanceKm() {
        return travelDistanceKm;
    }

    public void setTravelDistanceKm(Integer travelDistanceKm) {
        this.travelDistanceKm = travelDistanceKm;
    }

    public Boolean getNightShoot() {
        return nightShoot;
    }

    public void setNightShoot(Boolean nightShoot) {
        this.nightShoot = nightShoot;
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
