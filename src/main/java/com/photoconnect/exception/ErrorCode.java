package com.photoconnect.exception;

import org.springframework.http.HttpStatus;

/**
 * Standardized application error codes as defined in docs/ERROR_CODES.md.
 * Format: DOMAIN_XXX_DESCRIPTION
 */
public enum ErrorCode {

    // ── AUTH ─────────────────────────────────────────────────────────────────
    AUTH_001_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    AUTH_002_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email is already in use"),
    AUTH_003_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid authentication token"),
    AUTH_004_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Authentication token has expired"),
    AUTH_005_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied: insufficient permissions"),

    // ── USER ─────────────────────────────────────────────────────────────────
    USER_001_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    USER_002_ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Account is locked or suspended"),
    USER_003_INVALID_ROLE(HttpStatus.BAD_REQUEST, "Invalid user role specified"),

    // ── PHOTOGRAPHER ─────────────────────────────────────────────────────────
    PHOTOGRAPHER_001_NOT_FOUND(HttpStatus.NOT_FOUND, "Photographer profile not found"),
    PHOTOGRAPHER_002_NOT_APPROVED(HttpStatus.FORBIDDEN, "Photographer is not approved by administrator"),
    PHOTOGRAPHER_003_PROFILE_EXISTS(HttpStatus.CONFLICT, "Photographer profile already exists for this user"),
    PHOTOGRAPHER_004_NOT_OWNER(HttpStatus.FORBIDDEN, "You do not own this photographer profile"),

    // ── PORTFOLIO ────────────────────────────────────────────────────────────
    PORTFOLIO_001_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "Image file is required"),
    PORTFOLIO_002_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "Invalid file type. Only JPEG, PNG, and WebP are accepted"),
    PORTFOLIO_003_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload portfolio image to storage"),
    PORTFOLIO_004_NOT_FOUND(HttpStatus.NOT_FOUND, "Portfolio image not found"),

    // ── PACKAGE ──────────────────────────────────────────────────────────────
    PACKAGE_001_NOT_FOUND(HttpStatus.NOT_FOUND, "Service package not found"),
    PACKAGE_002_INACTIVE(HttpStatus.BAD_REQUEST, "Service package is inactive"),
    PACKAGE_003_INVALID_PRICE(HttpStatus.BAD_REQUEST, "Invalid package price specified"),

    // ── BOOKING ──────────────────────────────────────────────────────────────
    BOOKING_001_NOT_FOUND(HttpStatus.NOT_FOUND, "Booking not found"),
    BOOKING_002_TIME_CONFLICT(HttpStatus.CONFLICT, "Photographer is not available at this time"),
    BOOKING_003_INVALID_STATUS(HttpStatus.BAD_REQUEST, "Invalid booking status transition"),
    BOOKING_004_PAST_TIME(HttpStatus.BAD_REQUEST, "Booking time cannot be in the past"),
    BOOKING_005_NOT_OWNER(HttpStatus.FORBIDDEN, "Booking does not belong to current user"),
    BOOKING_006_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "Booking cannot be cancelled in its current state"),
    BOOKING_007_SELF_BOOKING(HttpStatus.BAD_REQUEST, "Photographer cannot book their own service"),

    // ── REVIEW ───────────────────────────────────────────────────────────────
    REVIEW_001_BOOKING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "Only completed bookings can be reviewed"),
    REVIEW_002_ALREADY_EXISTS(HttpStatus.CONFLICT, "A review has already been submitted for this booking"),
    REVIEW_003_INVALID_RATING(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5"),
    REVIEW_004_NOT_OWNER(HttpStatus.FORBIDDEN, "Customer does not own this booking"),

    // ── CHAT ─────────────────────────────────────────────────────────────────
    CHAT_001_BOOKING_REQUIRED(HttpStatus.BAD_REQUEST, "Chat message must be associated with a valid booking"),
    CHAT_002_ACCESS_DENIED(HttpStatus.FORBIDDEN, "You are not a participant in this booking chat"),
    CHAT_003_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "Message content cannot be blank"),
    CHAT_004_MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "Message exceeds maximum allowed length"),

    // ── ADMIN ────────────────────────────────────────────────────────────────
    ADMIN_001_CANNOT_MODIFY_SELF(HttpStatus.BAD_REQUEST, "Administrator cannot modify their own account status"),
    ADMIN_002_INVALID_APPROVAL_STATUS(HttpStatus.BAD_REQUEST, "Invalid photographer approval status"),

    // ── SYSTEM ───────────────────────────────────────────────────────────────
    SYSTEM_001_DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred"),
    SYSTEM_002_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred"),
    SYSTEM_003_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed for submitted data");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getCode() {
        return name();
    }
}
