package com.photoconnect.exception;

/** Signals that an authenticated photographer account has no Studio profile to aggregate. */
public class PhotographerStudioProfileNotFoundException extends RuntimeException {
    public PhotographerStudioProfileNotFoundException(String message) {
        super(message);
    }
}
