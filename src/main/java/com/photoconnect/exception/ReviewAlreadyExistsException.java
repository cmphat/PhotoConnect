package com.photoconnect.exception;

public class ReviewAlreadyExistsException extends PhotoConnectException {

    public ReviewAlreadyExistsException(String message) {
        super(ErrorCode.REVIEW_002_ALREADY_EXISTS, message);
    }
}
