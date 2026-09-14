package com.photoconnect.exception;

public class SelfBookingNotAllowedException extends PhotoConnectException {

    public SelfBookingNotAllowedException(String message) {
        super(ErrorCode.BOOKING_007_SELF_BOOKING, message);
    }
}
