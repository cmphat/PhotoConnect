package com.photoconnect.exception;

public class SelfBookingNotAllowedException extends RuntimeException {
    public SelfBookingNotAllowedException(String message) {
        super(message);
    }
}
