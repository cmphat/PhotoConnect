package com.photoconnect.exception;

public class InvalidStatusTransitionException extends PhotoConnectException {

    public InvalidStatusTransitionException(String message) {
        super(ErrorCode.BOOKING_003_INVALID_STATUS, message);
    }
}
