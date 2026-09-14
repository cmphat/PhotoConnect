package com.photoconnect.exception;

public class InvalidCredentialsException extends PhotoConnectException {

    public InvalidCredentialsException(String message) {
        super(ErrorCode.AUTH_001_INVALID_CREDENTIALS, message);
    }
}
