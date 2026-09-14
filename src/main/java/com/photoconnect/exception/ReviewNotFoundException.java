package com.photoconnect.exception;

public class ReviewNotFoundException extends ResourceNotFoundException {

    public ReviewNotFoundException(String message) {
        super(message);
    }
}
