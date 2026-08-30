package com.photoconnect.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PhotographerProfileRequest {

    @NotBlank(message = "Display name is required.")
    @Size(min = 2, max = 150, message = "Display name must be between 2 and 150 characters.")
    private String displayName;

    @NotBlank(message = "Bio is required.")
    @Size(min = 20, max = 2000, message = "Bio must be between 20 and 2000 characters.")
    private String bio;

    @NotBlank(message = "City is required.")
    @Size(max = 100, message = "City must not exceed 100 characters.")
    private String city;

    @NotNull(message = "Years of experience is required.")
    @Min(value = 0, message = "Experience years must be 0 or more.")
    @Max(value = 80, message = "Experience years must be 80 or less.")
    private Integer experienceYears;

    @NotNull(message = "Starting price is required.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Starting price must be zero or positive.")
    private BigDecimal priceFrom;

    public PhotographerProfileRequest() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(BigDecimal priceFrom) {
        this.priceFrom = priceFrom;
    }
}
