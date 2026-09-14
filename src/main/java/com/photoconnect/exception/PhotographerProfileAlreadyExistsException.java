package com.photoconnect.exception;

public class PhotographerProfileAlreadyExistsException extends PhotoConnectException {

    public PhotographerProfileAlreadyExistsException(String message) {
        super(ErrorCode.PHOTOGRAPHER_003_PROFILE_EXISTS, message);
    }
}
