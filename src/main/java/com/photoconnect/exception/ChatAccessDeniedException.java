package com.photoconnect.exception;

public class ChatAccessDeniedException extends PhotoConnectException {

    public ChatAccessDeniedException(String message) {
        super(ErrorCode.CHAT_002_ACCESS_DENIED, message);
    }
}
