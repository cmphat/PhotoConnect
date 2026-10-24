package com.photoconnect.dto;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @Size(max = 150, message = "Display name cannot exceed 150 characters")
    private String displayName;

    @Size(max = 100, message = "Username cannot exceed 100 characters")
    private String username;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    private String websiteUrl;

    @Size(max = 255, message = "Facebook URL cannot exceed 255 characters")
    private String facebookUrl;

    @Size(max = 255, message = "Instagram URL cannot exceed 255 characters")
    private String instagramUrl;

    @Size(max = 255, message = "TikTok URL cannot exceed 255 characters")
    private String tiktokUrl;

    @Size(max = 255, message = "YouTube URL cannot exceed 255 characters")
    private String youtubeUrl;

    public UserProfileUpdateRequest() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = normalizeUrl(websiteUrl);
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = normalizeUrl(facebookUrl);
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = normalizeUrl(instagramUrl);
    }

    public String getTiktokUrl() {
        return tiktokUrl;
    }

    public void setTiktokUrl(String tiktokUrl) {
        this.tiktokUrl = normalizeUrl(tiktokUrl);
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = normalizeUrl(youtubeUrl);
    }

    public static String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:")) {
            throw new IllegalArgumentException("Invalid URL protocol. Only HTTP and HTTPS are permitted.");
        }
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }
        return trimmed;
    }
}
