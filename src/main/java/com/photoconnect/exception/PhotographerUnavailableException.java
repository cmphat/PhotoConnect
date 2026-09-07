package com.photoconnect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PhotographerUnavailableException extends RuntimeException {
    public PhotographerUnavailableException(String message) {
        super(message);
    }
}
