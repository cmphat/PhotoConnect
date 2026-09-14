package com.photoconnect.exception;

/**
 * Thrown when an explicitly requested domain resource (user, photographer, booking, review) is not found.
 */
public class ResourceNotFoundException extends PhotoConnectException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.BOOKING_001_NOT_FOUND, message);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
