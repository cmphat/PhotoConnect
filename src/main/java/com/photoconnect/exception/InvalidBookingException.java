package com.photoconnect.exception;

public class InvalidBookingException extends PhotoConnectException {

    public InvalidBookingException(String message) {
        super(ErrorCode.BOOKING_001_NOT_FOUND, message);
    }

    public InvalidBookingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
