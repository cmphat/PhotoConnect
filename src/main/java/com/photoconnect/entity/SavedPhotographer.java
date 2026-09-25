package com.photoconnect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a saved / favorited photographer by a Customer user.
 */
@Entity
@Table(name = "saved_photographers",
       uniqueConstraints = {
           @UniqueConstraint(name = "UQ_saved_photographers_customer_profile",
                              columnNames = {"customer_id", "photographer_profile_id"})
       })
public class SavedPhotographer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photographer_profile_id", nullable = false)
    private PhotographerProfile photographerProfile;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SavedPhotographer() {
    }

    public SavedPhotographer(User customer, PhotographerProfile photographerProfile) {
        this.customer = customer;
        this.photographerProfile = photographerProfile;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedPhotographer that = (SavedPhotographer) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(customer, that.customer) &&
               Objects.equals(photographerProfile, that.photographerProfile);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(customer, photographerProfile);
    }
}
