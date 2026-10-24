package com.photoconnect.exception;

public class OtpMaxAttemptsException extends RuntimeException {
    public OtpMaxAttemptsException(String message) {
        super(message);
    }
}
