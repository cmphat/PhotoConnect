package com.photoconnect.exception;

public class UnauthorizedException extends PhotoConnectException {

    public UnauthorizedException(String message) {
        super(ErrorCode.AUTH_005_ACCESS_DENIED, message);
    }
}
