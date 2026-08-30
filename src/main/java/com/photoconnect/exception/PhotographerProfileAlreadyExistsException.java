package com.photoconnect.exception;

public class PhotographerProfileAlreadyExistsException extends RuntimeException {
    public PhotographerProfileAlreadyExistsException(String message) {
        super(message);
    }
}
