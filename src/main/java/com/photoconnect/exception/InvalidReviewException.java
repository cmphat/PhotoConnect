package com.photoconnect.exception;

public class InvalidReviewException extends PhotoConnectException {

    public InvalidReviewException(String message) {
        super(ErrorCode.REVIEW_003_INVALID_RATING, message);
    }
}
