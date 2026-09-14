package com.photoconnect.exception;

public class PhotographerUnavailableException extends PhotoConnectException {

    public PhotographerUnavailableException(String message) {
        super(ErrorCode.BOOKING_002_TIME_CONFLICT, message);
    }
}
