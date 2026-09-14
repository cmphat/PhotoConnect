package com.photoconnect.exception;

public class ReviewAccessDeniedException extends PhotoConnectException {

    public ReviewAccessDeniedException(String message) {
        super(ErrorCode.REVIEW_004_NOT_OWNER, message);
    }
}
