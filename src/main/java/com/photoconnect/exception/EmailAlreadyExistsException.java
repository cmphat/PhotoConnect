package com.photoconnect.exception;

public class EmailAlreadyExistsException extends PhotoConnectException {

    public EmailAlreadyExistsException(String message) {
        super(ErrorCode.AUTH_002_EMAIL_ALREADY_EXISTS, message);
    }
}
