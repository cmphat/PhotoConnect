package com.photoconnect.exception;

public class PasswordMismatchException extends PhotoConnectException {

    public PasswordMismatchException(String message) {
        super(ErrorCode.SYSTEM_003_VALIDATION_ERROR, message);
    }
}
