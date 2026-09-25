package com.photoconnect.security;

/** Deliberately detail-free exception so token parsing failures cannot leak crypto information. */
public class JwtValidationException extends RuntimeException {
    public JwtValidationException() {
        super("Invalid authentication token");
    }

    public JwtValidationException(Throwable cause) {
        super("Invalid authentication token", cause);
    }
}
