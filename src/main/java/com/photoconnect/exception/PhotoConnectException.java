package com.photoconnect.exception;

/**
 * Root unchecked exception for PhotoConnect domain business errors.
 * Encapsulates a standardized ErrorCode.
 */
public class PhotoConnectException extends RuntimeException {

    private final ErrorCode errorCode;

    public PhotoConnectException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public PhotoConnectException(ErrorCode errorCode, String message) {
        super(message != null ? message : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public PhotoConnectException(ErrorCode errorCode, String message, Throwable cause) {
        super(message != null ? message : errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
