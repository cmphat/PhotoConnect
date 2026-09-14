package com.photoconnect.exception;

public class AccountDisabledException extends PhotoConnectException {

    public AccountDisabledException(String message) {
        super(ErrorCode.USER_002_ACCOUNT_LOCKED, message);
    }
}
