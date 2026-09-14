package com.photoconnect.exception;

public class BookingNotCompletedException extends PhotoConnectException {

    public BookingNotCompletedException(String message) {
        super(ErrorCode.REVIEW_001_BOOKING_NOT_COMPLETED, message);
    }
}
